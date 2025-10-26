package dev.challenge.servicea.adapters;

import dev.challenge.common.dto.OrderDTO;
import dev.challenge.common.dto.OrderReplicaDTO;
import dev.challenge.servicea.domain.Delivery;
import dev.challenge.servicea.domain.Order;
import dev.challenge.servicea.domain.User;

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
        Order order = Order.builder()
                .externalId(UUID.randomUUID().toString())
                .build();
        updateMutableFields(dto, order, user, delivery);
        return order;
    }




    public static void updateEntityFromDto(OrderDTO dto, Order order, User user, Delivery delivery) {
        updateMutableFields(dto, order, user, delivery);
    }

    private static void updateMutableFields(OrderDTO dto, Order order, User user, Delivery delivery) {
        order.setDescription(dto.description());
        order.setValue(dto.value());
        order.setIdUser(user.getId());
        order.setIdDelivery(delivery.getId());
        order.setExternalUserId(user.getExternalId());
    }

    public static OrderReplicaDTO toReplica(Order order, Delivery delivery, User user) {
        return new OrderReplicaDTO(
                order.getId(),
                order.getDescription(),
                order.getValue(),
                order.getExternalId(),
                delivery.getName(),
                delivery.getPhone() ,
                user.getExternalId()
        );
    }
}
