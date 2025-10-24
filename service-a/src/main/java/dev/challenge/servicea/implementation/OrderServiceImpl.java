package dev.challenge.servicea.implementation;

import dev.challenge.common.dto.OrderDTO;
import dev.challenge.common.dto.OrderReplicaDTO;
import dev.challenge.common.error.CustomException;
import dev.challenge.common.replication.Replication;
import dev.challenge.servicea.adapters.OrderAdapter;
import dev.challenge.servicea.domain.Delivery;
import dev.challenge.servicea.domain.Order;
import dev.challenge.servicea.feign.OrderBClient;
import dev.challenge.servicea.repo.DeliveryRepository;
import dev.challenge.servicea.repo.OrderRepository;
import dev.challenge.servicea.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final OrderBClient client;

    @Override
    @Transactional
    public OrderDTO createOrder(OrderDTO dto) {
        log.info("Criando pedido...");
        validate(dto);
        Delivery delivery = requireDelivery(dto.idDelivery());
        Order saved = orderRepository.save(OrderAdapter.toNewEntity(dto));
        log.info("Pedido criado. id={}, externalId={}", saved.getId(), saved.getExternalId());
        if (!Replication.incoming()) {
            replicateCreate(saved, delivery);
        }
        return OrderAdapter.toOrderDTO(saved);
    }

    @Override
    @Transactional
    public OrderDTO updateOrder(String externalId, OrderDTO dto) {
        log.info("Atualizando pedido... externalId={}", externalId);
        validate(dto);

        Order found = orderRepository.findByExternalId(externalId)
                .orElseThrow(() -> {
                    log.warn("Pedido não encontrado para update. externalId={}", externalId);
                    return new CustomException(HttpStatus.NOT_FOUND, "Pedido não encontrado (ID: " + externalId + ")");
                });

        OrderAdapter.updateEntityFromDto(dto, found);
        Order saved = orderRepository.save(found);

        Delivery delivery = requireDelivery(saved.getIdDelivery());

        if (!Replication.incoming()) {
            replicateUpdate(externalId, saved, delivery);
        }

        log.info("Pedido atualizado. id={}, externalId={}", saved.getId(), saved.getExternalId());
        return OrderAdapter.toOrderDTO(saved);
    }

    @Override
    @Transactional
    public void deleteOrder(String externalId) {
        log.info("Removendo pedido... externalId={}", externalId);

        boolean exists = orderRepository.existsByExternalId(externalId);
        if (!exists) {
            log.warn("Pedido não encontrado para remoção. externalId={}", externalId);
            throw new CustomException(HttpStatus.NOT_FOUND, "Pedido não encontrado (ID: " + externalId + ")");
        }

        orderRepository.deleteByExternalId(externalId);

        if (!Replication.incoming()) {
            replicateDelete(externalId);
        }

        log.info("Pedido removido. externalId={}", externalId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> listOrders() {
        log.info("Listando pedidos...");
        List<OrderDTO> list = orderRepository.findAll().stream()
                .map(OrderAdapter::toOrderDTO)
                .toList();
        log.info("Listagem de pedidos concluída. total={}", list.size());
        return list;
    }

    private Delivery requireDelivery(Long idDelivery) {
        return deliveryRepository.findById(idDelivery)
                .orElseThrow(() -> {
                    log.warn("Entregador não encontrado. id={}", idDelivery);
                    return new CustomException(HttpStatus.NOT_FOUND,
                            "Entregador não encontrado (ID: " + idDelivery + ")");
                });
    }

    private void replicateCreate(Order saved, Delivery delivery) {
        try {
            client.createOrder(toReplica(saved, delivery));
            log.info("Replicação create enviada para Service B. externalId={}", saved.getExternalId());
        } catch (Exception e) {
            log.error("Falha ao replicar pedido (create) para Service B. externalId={}, err={}",
                    saved.getExternalId(), e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY,
                    "Falha ao replicar pedido para o Service B");
        }
    }

    private void replicateUpdate(String externalId, Order saved, Delivery delivery) {
        try {
            client.updateOrder(externalId, toReplica(saved, delivery));
            log.info("Replicação update enviada para Service B. externalId={}", externalId);
        } catch (Exception e) {
            log.error("Falha ao replicar pedido (update) para Service B. externalId={}, err={}",
                    externalId, e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY,
                    "Falha ao replicar atualização de pedido para o Service B (ID: " + externalId + ")");
        }
    }

    private void replicateDelete(String externalId) {
        try {
            client.deleteOrder(externalId);
            log.info("Replicação delete enviada para Service B. externalId={}", externalId);
        } catch (Exception e) {
            log.error("Falha ao replicar pedido (delete) para Service B. externalId={}, err={}",
                    externalId, e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY,
                    "Falha ao replicar remoção de pedido para o Service B (ID: " + externalId + ")");
        }
    }

    private static OrderReplicaDTO toReplica(Order saved, Delivery delivery) {
        return new OrderReplicaDTO(
                saved.getId(),
                saved.getDescription(),
                saved.getValue(),
                saved.getIdUser(),
                saved.getExternalId(),
                delivery.getName(),
                delivery.getPhone()
        );
    }

    private static void validate(OrderDTO dto) {
        if (dto == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Payload obrigatório ausente.");
        }
        if (isBlank(dto.description())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Campo 'description' é obrigatório.");
        }
        if (dto.value() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Campo 'value' é obrigatório.");
        }
        if (dto.idUser() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Campo 'idUser' é obrigatório.");
        }
        if (dto.idDelivery() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Campo 'idDelivery' é obrigatório.");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
