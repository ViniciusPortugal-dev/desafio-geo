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
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;
    private final OrderBClient client;
    private final Force422 force422;

    private User requireUserById(String idUser) {
        return userRepository.findByExternalId(idUser)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Usuário não encontrado (ID: " + idUser + ")"));
    }

    @Override
    @Transactional
    public OrderDTO createOrder(OrderDTO dto) {
        validate(dto);
        Delivery delivery = requireDelivery(dto.idDelivery());
        User user = requireUserById(dto.externalUserId());
        Order saved = orderRepository.save(OrderAdapter.toNewEntity(dto, user, delivery));
        if (!Replication.incoming()) {
            force422.registerLocalCreateSuccess();
            if (force422.shouldForceNext422AndReset()) {
                throw new CustomException(HttpStatus.UNPROCESSABLE_ENTITY, "Exceção intencional para avaliação (case 5.1)");
            }
            client.createOrder(OrderAdapter.toReplica(saved, delivery, user));
        }
        return OrderAdapter.toOrderDTO(saved);
    }

    @Override
    @Transactional
    public OrderDTO updateOrder(String externalId, OrderDTO dto) {
        validateExternalId(externalId);
        validate(dto);
        User user = requireUserById(dto.externalUserId());
        Order found = orderRepository.findByExternalId(externalId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Pedido não encontrado (ID: " + externalId + ")"));
        Delivery delivery = requireDelivery(dto.idDelivery());
        OrderAdapter.updateEntityFromDto(dto, found, user, delivery);
        Order saved = orderRepository.save(found);
        if (!Replication.incoming()) {
            client.updateOrder(externalId, OrderAdapter.toReplica(saved, delivery, user));
        }
        return OrderAdapter.toOrderDTO(saved);
    }

    @Override
    @Transactional
    public void deleteOrder(String externalId) {
        log.info("Removendo pedido... externalId={}", externalId);
        validateExternalId(externalId);

        boolean exists = orderRepository.existsByExternalId(externalId);
        if (!exists) {
            log.warn("Pedido não encontrado para remoção. externalId={}", externalId);
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
            log.error("Erro ao remover pedido. externalId={}", externalId, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao remover pedido: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> listOrders() {
        log.info("Listando pedidos...");
        try {
            List<OrderDTO> list = orderRepository.findAll().stream()
                    .map(OrderAdapter::toOrderDTO)
                    .toList();

            if (list.isEmpty()) {
                log.warn("Nenhum pedido encontrado na listagem.");
                throw new CustomException(HttpStatus.NO_CONTENT, "Nenhum pedido encontrado.");
            }

            log.info("Listagem de pedidos concluída. total={}", list.size());
            return list;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao listar pedidos.", e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao listar pedidos: " + e.getMessage());
        }
    }

    private Delivery requireDelivery(Long idDelivery) {
        return deliveryRepository.findById(idDelivery)
                .orElseThrow(() -> {
                    log.warn("Entregador não encontrado. id={}", idDelivery);
                    return new CustomException(HttpStatus.NOT_FOUND,
                            "Entregador não encontrado (ID: " + idDelivery + ")");
                });
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
        if (dto.externalUserId() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Campo 'externalUserId' é obrigatório.");
        }
        if (dto.idDelivery() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Campo 'idDelivery' é obrigatório.");
        }
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
