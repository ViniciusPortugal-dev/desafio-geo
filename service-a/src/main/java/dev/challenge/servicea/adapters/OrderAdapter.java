// service-a
package dev.challenge.servicea.adapters;

import dev.challenge.common.dto.OrderDTO;
import dev.challenge.common.dto.OrderReplicaDTO;
import dev.challenge.servicea.domain.Delivery;
import dev.challenge.servicea.domain.Order;
import dev.challenge.servicea.domain.User;

import java.util.Objects;
import java.util.UUID;

public final class OrderAdapter {

    private OrderAdapter(){}

    public static OrderDTO toOrderDTO(Order entity) { if (entity == null) return null;
        return OrderDTO.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .value(entity.getValue())
                .idDelivery(entity.getIdDelivery())
                .externalId(entity.getExternalId())
                .externalUserId(entity.getExternalUserId())
                .build();
    }

    public static Order toNewEntity(OrderDTO dto, User user, Delivery delivery) {
        Objects.requireNonNull(dto, "OrderDTO não pode ser nulo");
        Order e = new Order();
        e.setExternalId(UUID.randomUUID().toString());
        updateMutableFields(dto, e, user, delivery);
        return e;
    }



    public static void updateEntityFromDto(OrderDTO dto, Order e, User user, Delivery delivery) {
        Objects.requireNonNull(e, "Order entity não pode ser nula");
        if (dto == null) return;
        updateMutableFields(dto, e, user, delivery);
    }

    private static void updateMutableFields(OrderDTO dto, Order e, User user, Delivery delivery) {
        e.setDescription(dto.description());
        e.setValue(dto.value());
        e.setIdUser(user.getId());
        e.setIdDelivery(delivery.getId());
        e.setExternalUserId(user.getExternalId());
    }

    public static OrderReplicaDTO toReplica(Order order, Delivery delivery, User user) {
        return new OrderReplicaDTO(
                order.getId(),
                order.getDescription(),
                order.getValue(),
                order.getExternalId(),
                delivery != null ? delivery.getName() : null,
                delivery != null ? delivery.getPhone() : null,
                user.getExternalId()
        );
    }
}
