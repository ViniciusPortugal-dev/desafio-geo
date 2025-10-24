package dev.challenge.serviceb.adapters;

import dev.challenge.common.dto.OrderReplicaDTO;
import dev.challenge.serviceb.domain.Order;

import java.util.Objects;

public final class OrderAdapter {

    private OrderAdapter() {}

    public static OrderReplicaDTO toOrderDTO(Order e) {
        if (e == null) return null;
        return new OrderReplicaDTO(
                e.getId(),
                e.getDescription(),
                e.getValue(),
                e.getIdUser(),
                e.getExternalId(),
                e.getDeliveryName(),
                e.getPhoneDelivery()
        );
    }

    public static Order toNewEntity(OrderReplicaDTO dto) {
        Objects.requireNonNull(dto, "OrderReplicaDTO não pode ser nulo");
        Order e = new Order();
        updateMutableFields(dto, e);
        e.setExternalId(dto.externalId());
        return e;
    }

    public static void updateEntityFromDto(OrderReplicaDTO dto, Order e) {
        Objects.requireNonNull(e, "Order entity não pode ser nula");
        if (dto == null) return;
        updateMutableFields(dto, e);
    }

    private static void updateMutableFields(OrderReplicaDTO dto, Order e) {
        e.setDescription(dto.description());
        e.setValue(dto.value());
        e.setIdUser(dto.idUser());
        e.setDeliveryName(dto.deliveryName());
        e.setPhoneDelivery(dto.deliveryPhone());
    }
}
