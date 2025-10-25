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
        log.info("Listando pedidos (Service B)...");
        try {
            List<OrderReplicaDTO> list = orderRepository.findAll()
                    .stream()
                    .map(o -> {
                        User u = userRepository.findById(o.getIdUser()).orElse(null);
                        return OrderAdapter.toOrderDTO(o, u);
                    })
                    .toList();

            if (list.isEmpty()) {
                log.warn("Nenhum pedido encontrado na listagem (Service B).");
                throw new CustomException(HttpStatus.NO_CONTENT, "Nenhum pedido encontrado.");
            }

            log.info("Listagem concluída. total={}", list.size());
            return list;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao listar pedidos (Service B).", e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao listar pedidos: " + e.getMessage());
        }
    }


    private User requireUserByExternalId(String userExternalId) {
        return userRepository.findByExternalId(userExternalId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,
                        "Usuário não encontrado (externalId: " + userExternalId + ")"));
    }


    @Override
    @Transactional
    public OrderReplicaDTO createOrder(OrderReplicaDTO dto) {
        User user = requireUserByExternalId(dto.externalUserId());
        Order saved = orderRepository.save(OrderAdapter.toNewEntity(dto, user));
        OrderReplicaDTO out = OrderAdapter.toOrderDTO(saved, user);
        if (!Replication.incoming()) {
            replicateCreate(out);
        }
        return out;
    }


    @Override
    @Transactional
    public OrderReplicaDTO updateOrder(String externalId, OrderReplicaDTO dto) {
        validateExternalId(externalId);
        validate(dto);

        User user = requireUserByExternalId(dto.externalUserId());
        Order found = orderRepository.findByExternalId(externalId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Pedido não encontrado (ID: " + externalId + ")"));
        Order saved = orderRepository.save(OrderAdapter.updateEntityFromDto(dto, found, user));
        OrderReplicaDTO out = OrderAdapter.toOrderDTO(saved, user);
        if (!Replication.incoming()) replicateUpdate(externalId, out);
        return out;
    }



    @Override
    @Transactional
    public void deleteOrder(String externalId) {
        log.info("Removendo pedido (Service B)... externalId={}", externalId);
        validateExternalId(externalId);

        boolean exists = orderRepository.existsByExternalId(externalId);
        if (!exists) {
            log.warn("Pedido não encontrado para remoção (Service B). externalId={}", externalId);
            throw new CustomException(HttpStatus.NOT_FOUND, "Pedido não encontrado (ID: " + externalId + ")");
        }

        try {
            orderRepository.deleteByExternalId(externalId);

            if (!Replication.incoming()) {
                replicateDelete(externalId);
            }

            log.info("Pedido removido. externalId={}", externalId);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao remover pedido (Service B). externalId={}", externalId, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao remover pedido: " + e.getMessage());
        }
    }

    private void replicateCreate(OrderReplicaDTO out) {
        try {
            orderAClient.createOrder(out);
            log.info("Replicação create enviada ao Service A. externalId={}", out.externalId());
        } catch (Exception e) {
            log.error("Falha ao replicar pedido (create) ao Service A. externalId={}, err={}", out.externalId(), e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY, "Falha ao replicar pedido para o Service A");
        }
    }

    private void replicateUpdate(String externalId, OrderReplicaDTO out) {
        try {
            orderAClient.updateOrder(externalId, out);
            log.info("Replicação update enviada ao Service A. externalId={}", externalId);
        } catch (Exception e) {
            log.error("Falha ao replicar pedido (update) ao Service A. externalId={}, err={}", externalId, e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY, "Falha ao replicar atualização de pedido para o Service A (ID: " + externalId + ")");
        }
    }

    private void replicateDelete(String externalId) {
        try {
            orderAClient.deleteOrder(externalId);
            log.info("Replicação delete enviada ao Service A. externalId={}", externalId);
        } catch (Exception e) {
            log.error("Falha ao replicar pedido (delete) ao Service A. externalId={}, err={}", externalId, e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY, "Falha ao replicar exclusão de pedido para o Service A (ID: " + externalId + ")");
        }
    }

    private static void validate(OrderReplicaDTO dto) {
        if (dto == null) throw new CustomException(HttpStatus.BAD_REQUEST, "Payload obrigatório ausente.");
        if (isBlank(dto.description())) throw new CustomException(HttpStatus.BAD_REQUEST, "Campo 'description' é obrigatório.");
        if (dto.value() == null) throw new CustomException(HttpStatus.BAD_REQUEST, "Campo 'value' é obrigatório.");
        if (isBlank(dto.externalUserId())) throw new CustomException(HttpStatus.BAD_REQUEST, "Campo 'userExternalId' é obrigatório.");
    }


    private static void validateExternalId(String externalId) {
        if (isBlank(externalId)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Parâmetro 'externalId' é obrigatório.");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
