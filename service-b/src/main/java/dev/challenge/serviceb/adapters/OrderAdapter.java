package dev.challenge.serviceb.adapters;

import dev.challenge.common.dto.OrderReplicaDTO;
import dev.challenge.serviceb.domain.Order;
import dev.challenge.serviceb.domain.User;

import java.util.Objects;
import java.util.UUID;

public final class OrderAdapter {

    private OrderAdapter() {}

    public static OrderReplicaDTO toOrderDTO(Order e, User user) {
        if (e == null) return null;
        return new OrderReplicaDTO(
                e.getId(),
                e.getDescription(),
                e.getValue(),
                e.getExternalId(),
                e.getDeliveryName(),
                e.getPhoneDelivery(),
                user.getExternalId()
        );
    }

    public static Order toNewEntity(OrderReplicaDTO dto, User user) {
        Objects.requireNonNull(dto, "OrderReplicaDTO não pode ser nulo");
        Order e = new Order();
        e.setExternalId(dto.externalId() != null ? dto.externalId() : UUID.randomUUID().toString());
        updateMutableFields(dto, e, user);
        return e;
    }

    public static Order updateEntityFromDto(OrderReplicaDTO dto, Order e, User user) {
        Objects.requireNonNull(e, "Order entity não pode ser nula");
        return updateMutableFields(dto, e, user);
    }

    private static Order updateMutableFields(OrderReplicaDTO dto, Order e, User user) {
        e.setDescription(dto.description());
        e.setValue(dto.value());
        e.setIdUser(user.getId());
        e.setDeliveryName(dto.deliveryName());
        e.setPhoneDelivery(dto.deliveryPhone());
        e.setExternalUserId(user.getExternalId());
        return e;
    }
}
