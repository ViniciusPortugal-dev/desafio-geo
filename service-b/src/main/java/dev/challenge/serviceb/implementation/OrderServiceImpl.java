package dev.challenge.serviceb.implementation;

import dev.challenge.common.dto.OrderReplicaDTO;
import dev.challenge.common.error.CustomException;
import dev.challenge.common.replication.Replication;
import dev.challenge.serviceb.adapters.OrderAdapter;
import dev.challenge.serviceb.domain.Order;
import dev.challenge.serviceb.domain.User;
import dev.challenge.serviceb.feign.OrderAClient;
import dev.challenge.serviceb.repo.OrderRepository;
import dev.challenge.serviceb.repo.UserRepository;
import dev.challenge.serviceb.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderAClient orderAClient;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OrderReplicaDTO> listOrders() {
        log.info("Listing orders (Service B)...");
        try {
            List<OrderReplicaDTO> result = orderRepository.findAll()
                    .stream()
                    .map(orderEntity -> {
                        String externalUserId = Objects.requireNonNull(userRepository.findById(orderEntity.getIdUser()).orElse(null)).getExternalId();
                        return OrderAdapter.toOrderDTO(orderEntity, externalUserId);
                    })
                    .toList();

            if (result.isEmpty()) {
                log.warn("No orders found (Service B).");
                throw new CustomException(HttpStatus.NO_CONTENT, "No orders found.");
            }

            log.info("Listing completed. total={}", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error listing orders (Service B).", e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error listing orders");
        }
    }

    private User requireUserByExternalId(String userExternalId) {
        return userRepository.findByExternalId(userExternalId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,
                        "User not found (externalId: " + userExternalId + ")"));
    }

    @Override
    @Transactional
    public OrderReplicaDTO createOrder(OrderReplicaDTO orderDTO) {
        User userEntity = requireUserByExternalId(orderDTO.externalUserId());

        log.info("Creating order (Service B)... externalUserId={}", orderDTO.externalUserId());
        try {
            Order savedEntity = orderRepository.save(OrderAdapter.toNewEntity(orderDTO, userEntity));
            OrderReplicaDTO out = OrderAdapter.toOrderDTO(savedEntity, userEntity.getExternalId());

            if (!Replication.incoming()) {
                replicateCreate(out);
            }

            log.info("Order created (Service B). externalId={}", savedEntity.getExternalId());
            return out;
        } catch (Exception e) {
            log.error("Error creating order (Service B). payload={}", orderDTO, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating order");
        }
    }

    @Override
    @Transactional
    public OrderReplicaDTO updateOrder(String externalId, OrderReplicaDTO orderDTO) {
        User userEntity = requireUserByExternalId(orderDTO.externalUserId());
        Order foundEntity = orderRepository.findByExternalId(externalId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,
                        "Order not found (externalId: " + externalId + ")"));

        log.info("Updating order (Service B)... externalId={}", externalId);
        try {
            Order updatedEntity = OrderAdapter.updateEntityFromDto(orderDTO, foundEntity, userEntity);
            Order savedEntity = orderRepository.save(updatedEntity);

            OrderReplicaDTO out = OrderAdapter.toOrderDTO(savedEntity, userEntity.getExternalId());
            if (!Replication.incoming()) {
                replicateUpdate(externalId, out);
            }

            log.info("Order updated (Service B). externalId={}", savedEntity.getExternalId());
            return out;
        } catch (Exception e) {
            log.error("Error updating order (Service B). externalId={}", externalId, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating order");
        }
    }

    @Override
    @Transactional
    public void deleteOrder(String externalId) {
        log.info("Deleting order (Service B)... externalId={}", externalId);
        boolean exists = orderRepository.existsByExternalId(externalId);
        if (!exists) {
            log.warn("Order not found for deletion (Service B). externalId={}", externalId);
            throw new CustomException(HttpStatus.NOT_FOUND, "Order not found (externalId: " + externalId + ")");
        }

        try {
            orderRepository.deleteByExternalId(externalId);

            if (!Replication.incoming()) {
                replicateDelete(externalId);
            }

            log.info("Order deleted (Service B). externalId={}", externalId);
        } catch (Exception e) {
            log.error("Error deleting order (Service B). externalId={}", externalId, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting order");
        }
    }

    private void replicateCreate(OrderReplicaDTO out) {
        try {
            orderAClient.createOrder(out);
            log.info("Create replication sent to Service A. externalId={}", out.externalId());
        } catch (Exception e) {
            log.error("Failed to replicate order (create) to Service A. externalId={}, err={}",
                    out.externalId(), e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY, "Failed to replicate order to Service A");
        }
    }

    private void replicateUpdate(String externalId, OrderReplicaDTO out) {
        try {
            orderAClient.updateOrder(externalId, out);
            log.info("Update replication sent to Service A. externalId={}", externalId);
        } catch (Exception e) {
            log.error("Failed to replicate order (update) to Service A. externalId={}, err={}",
                    externalId, e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY,
                    "Failed to replicate order update to Service A (externalId: " + externalId + ")");
        }
    }

    private void replicateDelete(String externalId) {
        try {
            orderAClient.deleteOrder(externalId);
            log.info("Delete replication sent to Service A. externalId={}", externalId);
        } catch (Exception e) {
            log.error("Failed to replicate order (delete) to Service A. externalId={}, err={}",
                    externalId, e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY,
                    "Failed to replicate order deletion to Service A (externalId: " + externalId + ")");
        }
    }

}
