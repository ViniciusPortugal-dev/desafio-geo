package dev.challenge.serviceb.implementation;

import dev.challenge.common.dto.OrderReplicaDTO;
import dev.challenge.common.error.CustomException;
import dev.challenge.common.replication.Replication;
import dev.challenge.serviceb.adapters.OrderAdapter;
import dev.challenge.serviceb.domain.Order;
import dev.challenge.serviceb.feign.OrderAClient;
import dev.challenge.serviceb.repo.OrderRepository;
import dev.challenge.serviceb.services.OrderService;
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
    private final OrderAClient orderAClient;

    @Override
    @Transactional(readOnly = true)
    public List<OrderReplicaDTO> listOrders() {
        log.info("Listando pedidos (Service B)...");
        List<OrderReplicaDTO> list = orderRepository.findAll()
                .stream()
                .map(OrderAdapter::toOrderDTO)
                .toList();
        log.info("Listagem concluída. total={}", list.size());
        return list;
    }

    @Override
    @Transactional
    public OrderReplicaDTO createOrder(OrderReplicaDTO dto) {
        log.info("Criando pedido (Service B)...");
        validate(dto);
        Order saved = orderRepository.save(OrderAdapter.toNewEntity(dto));
        OrderReplicaDTO out = OrderAdapter.toOrderDTO(saved);
        log.info("Pedido criado. id={}, externalId={}", saved.getId(), saved.getExternalId());

        if (!Replication.incoming()) {
            replicateCreate(out);
        }

        return out;
    }

    @Override
    @Transactional
    public OrderReplicaDTO updateOrder(String externalId, OrderReplicaDTO dto) {
        log.info("Atualizando pedido... externalId={}", externalId);
        validate(dto);

        Order found = orderRepository.findByExternalId(externalId)
                .orElseThrow(() -> {
                    log.warn("Pedido não encontrado para update. externalId={}", externalId);
                    return new CustomException(HttpStatus.NOT_FOUND,
                            "Pedido não encontrado (ID: " + externalId + ")");
                });

        OrderAdapter.updateEntityFromDto(dto, found);
        Order saved = orderRepository.save(found);
        OrderReplicaDTO out = OrderAdapter.toOrderDTO(saved);

        if (!Replication.incoming()) {
            replicateUpdate(externalId, out);
        }

        log.info("Pedido atualizado. externalId={}", externalId);
        return out;
    }

    @Override
    @Transactional
    public void deleteOrder(String externalId) {
        log.info("Removendo pedido... externalId={}", externalId);

        boolean exists = orderRepository.existsByExternalId(externalId);
        if (!exists) {
            log.warn("Pedido não encontrado para remoção. externalId={}", externalId);
            throw new CustomException(HttpStatus.NOT_FOUND,
                    "Pedido não encontrado (ID: " + externalId + ")");
        }

        orderRepository.deleteByExternalId(externalId);

        if (!Replication.incoming()) {
            replicateDelete(externalId);
        }

        log.info("Pedido removido. externalId={}", externalId);
    }

    private void replicateCreate(OrderReplicaDTO out) {
        try {
            orderAClient.createOrder(out);
            log.info("Replicação create enviada ao Service A. externalId={}", out.externalId());
        } catch (Exception e) {
            log.error("Falha ao replicar pedido (create) ao Service A. externalId={}, err={}",
                    out.externalId(), e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY,
                    "Falha ao replicar pedido para o Service A");
        }
    }

    private void replicateUpdate(String externalId, OrderReplicaDTO out) {
        try {
            orderAClient.updateOrder(externalId, out);
            log.info("Replicação update enviada ao Service A. externalId={}", externalId);
        } catch (Exception e) {
            log.error("Falha ao replicar pedido (update) ao Service A. externalId={}, err={}",
                    externalId, e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY,
                    "Falha ao replicar atualização de pedido para o Service A (ID: " + externalId + ")");
        }
    }

    private void replicateDelete(String externalId) {
        try {
            orderAClient.deleteOrder(externalId);
            log.info("Replicação delete enviada ao Service A. externalId={}", externalId);
        } catch (Exception e) {
            log.error("Falha ao replicar pedido (delete) ao Service A. externalId={}, err={}",
                    externalId, e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY,
                    "Falha ao replicar exclusão de pedido para o Service A (ID: " + externalId + ")");
        }
    }

    private static void validate(OrderReplicaDTO dto) {
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
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
