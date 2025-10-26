package dev.challenge.servicea.implementation;

import dev.challenge.common.dto.OrderDTO;
import dev.challenge.common.error.CustomException;
import dev.challenge.common.replication.Replication;
import dev.challenge.servicea.adapters.OrderAdapter;
import dev.challenge.servicea.domain.Delivery;
import dev.challenge.servicea.domain.Order;
import dev.challenge.servicea.domain.User;
import dev.challenge.servicea.feign.OrderBClient;
import dev.challenge.servicea.replication.Force422;
import dev.challenge.servicea.repo.DeliveryRepository;
import dev.challenge.servicea.repo.OrderRepository;
import dev.challenge.servicea.repo.UserRepository;
import dev.challenge.servicea.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;
    private final OrderBClient client;
    private final Force422 force422;

    private User requireUserByExternalId(String externalUserId) {
        return userRepository.findByExternalId(externalUserId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,
                        "User not found (externalId: " + externalUserId + ")"));
    }

    private Delivery requireDelivery(Long idDelivery) {
        return deliveryRepository.findById(idDelivery)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,
                        "Delivery not found (id: " + idDelivery + ")"));
    }

    @Override
    @Transactional
    public OrderDTO createOrder(OrderDTO dto) {
        User user = requireUserByExternalId(dto.externalUserId());
        Delivery delivery = requireDelivery(dto.idDelivery());

        log.info("Creating order... externalUserId={}, deliveryId={}", dto.externalUserId(), dto.idDelivery());
        try {
            Order saved = orderRepository.save(OrderAdapter.toNewEntity(dto, user, delivery));

            if (!Replication.incoming()) {
                force422.registerLocalCreateSuccess();
                if (force422.shouldForceNext422AndReset()) {
                    throw new CustomException(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Intentional exception for evaluation (case 5.1)");
                }
                client.createOrder(OrderAdapter.toReplica(saved, delivery, user));
            }

            log.info("Order created. externalId={}", saved.getExternalId());
            return OrderAdapter.toOrderDTO(saved);
        } catch (Exception e) {
            log.error("Error creating order. payload={}", dto, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating order");
        }
    }

    @Override
    @Transactional
    public OrderDTO updateOrder(String externalId, OrderDTO dto) {
        User user = requireUserByExternalId(dto.externalUserId());
        Order found = orderRepository.findByExternalId(externalId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,
                        "Order not found (externalId: " + externalId + ")"));
        Delivery delivery = requireDelivery(dto.idDelivery());

        log.info("Updating order... externalId={}, externalUserId={}, deliveryId={}",
                externalId, dto.externalUserId(), dto.idDelivery());
        try {
            OrderAdapter.updateEntityFromDto(dto, found, user, delivery);
            Order saved = orderRepository.save(found);

            if (!Replication.incoming()) {
                client.updateOrder(externalId, OrderAdapter.toReplica(saved, delivery, user));
            }

            log.info("Order updated. externalId={}", saved.getExternalId());
            return OrderAdapter.toOrderDTO(saved);
        } catch (Exception e) {
            log.error("Error updating order. externalId={}", externalId, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating order");
        }
    }

    @Override
    @Transactional
    public void deleteOrder(String externalId) {
        log.info("Deleting order... externalId={}", externalId);

        boolean exists = orderRepository.existsByExternalId(externalId);
        if (!exists) {
            log.warn("Order not found for deletion. externalId={}", externalId);
            throw new CustomException(HttpStatus.NOT_FOUND,
                    "Order not found (externalId: " + externalId + ")");
        }

        try {
            orderRepository.deleteByExternalId(externalId);

            if (!Replication.incoming()) {
                replicateDelete(externalId);
            }

            log.info("Order deleted. externalId={}", externalId);
        } catch (Exception e) {
            log.error("Error deleting order. externalId={}", externalId, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting order");
        }
    }

    @Override
    public List<OrderDTO> listOrders() {
        log.info("Listing orders...");
        try {
            List<OrderDTO> list = orderRepository.findAll().stream()
                    .map(OrderAdapter::toOrderDTO)
                    .toList();

            if (list.isEmpty()) {
                log.warn("No orders found.");
                throw new CustomException(HttpStatus.NO_CONTENT, "No orders found.");
            }

            log.info("Listing completed. total={}", list.size());
            return list;
        } catch (Exception e) {
            log.error("Error listing orders.", e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error listing orders");
        }
    }

    private void replicateDelete(String externalId) {
        try {
            client.deleteOrder(externalId);
            log.info("Delete replication sent to Service B. externalId={}", externalId);
        } catch (Exception e) {
            log.error("Failed to replicate order deletion to Service B. externalId={}, err={}",
                    externalId, e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY,
                    "Failed to replicate order deletion to Service B (externalId: " + externalId + ")");
        }
    }
}

