package dev.challenge.serviceb.adapters;

import dev.challenge.common.dto.OrderReplicaDTO;
import dev.challenge.serviceb.domain.Order;
import dev.challenge.serviceb.domain.User;

import java.util.Objects;
import java.util.UUID;

public final class OrderAdapter {

    private OrderAdapter() {}

    public static OrderReplicaDTO toOrderDTO(Order orderEntity, String externalUserId) {
        return new OrderReplicaDTO(
                orderEntity.getId(),
                orderEntity.getDescription(),
                orderEntity.getValue(),
                orderEntity.getExternalId(),
                orderEntity.getDeliveryName(),
                orderEntity.getPhoneDelivery(),
                externalUserId
        );
    }

    public static Order toNewEntity(OrderReplicaDTO orderReplicaDTO, User userEntity) {
        Objects.requireNonNull(orderReplicaDTO, "OrderReplicaDTO must not be null");
        return Order.builder()
                .externalId(orderReplicaDTO.externalId() != null
                        ? orderReplicaDTO.externalId()
                        : UUID.randomUUID().toString())
                .description(orderReplicaDTO.description())
                .value(orderReplicaDTO.value())
                .idUser(userEntity.getId())
                .deliveryName(orderReplicaDTO.deliveryName())
                .phoneDelivery(orderReplicaDTO.deliveryPhone())
                .externalUserId(userEntity.getExternalId())
                .build();
    }

    public static Order updateEntityFromDto(OrderReplicaDTO orderReplicaDTO, Order orderEntity, User userEntity) {
        return updateMutableFields(orderReplicaDTO, orderEntity, userEntity);
    }

    private static Order updateMutableFields(OrderReplicaDTO orderReplicaDTO, Order orderEntity, User userEntity) {
        orderEntity.setDescription(orderReplicaDTO.description());
        orderEntity.setValue(orderReplicaDTO.value());
        orderEntity.setIdUser(userEntity.getId());
        orderEntity.setDeliveryName(orderReplicaDTO.deliveryName());
        orderEntity.setPhoneDelivery(orderReplicaDTO.deliveryPhone());
        orderEntity.setExternalUserId(userEntity.getExternalId());
        return orderEntity;
    }
}
