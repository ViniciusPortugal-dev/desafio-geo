package dev.challenge.servicea.adapters;

import dev.challenge.common.dto.DeliveryDTO;
import dev.challenge.servicea.domain.Delivery;

public final class DeliveryAdapter {

    private DeliveryAdapter() {}

    public static DeliveryDTO toDeliveryDTO(Delivery delivery) {
        return DeliveryDTO.builder()
                .id(delivery.getId())
                .name(delivery.getName())
                .phone(delivery.getPhone())
                .build();
    }

    public static Delivery toNewEntity(DeliveryDTO dto) {
        return Delivery.builder()
                .name(dto.name())
                .phone(dto.phone())
                .build();
    }

    public static void updateEntityFromDto(DeliveryDTO dto, Delivery entity) {
        entity.setName(dto.name());
        entity.setPhone(dto.phone());
    }

}
