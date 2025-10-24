package dev.challenge.servicea.adapters;

import dev.challenge.common.dto.DeliveryDTO;
import dev.challenge.servicea.domain.Delivery;

import java.util.Objects;

public final class DeliveryAdapter {

    private DeliveryAdapter() {}

    public static DeliveryDTO toDeliveryDTO(Delivery e) {
        if (e == null) return null;
        return DeliveryDTO.builder()
                .id(e.getId())
                .name(e.getName())
                .phone(e.getPhone())
                .build();
    }

    public static Delivery toNewEntity(DeliveryDTO dto) {
        if (dto == null) return null;
        return Delivery.builder()
                .name(safe(dto.name()))
                .phone(safe(dto.phone()))
                .build();
    }

    public static void updateEntityFromDto(DeliveryDTO dto, Delivery entity) {
        Objects.requireNonNull(entity, "Entity não pode ser nula");
        if (dto == null) return;
        entity.setName(safe(dto.name()));
        entity.setPhone(safe(dto.phone()));
    }

    private static String safe(String s) {
        return s == null ? null : s.trim();
    }
}
