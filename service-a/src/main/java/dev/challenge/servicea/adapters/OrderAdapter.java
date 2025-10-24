package dev.challenge.servicea.adapters;

import dev.challenge.common.dto.OrderDTO;
import dev.challenge.servicea.domain.Order;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public final class OrderAdapter {

    private OrderAdapter() {}

    public static OrderDTO toOrderDTO(Order entity) {
        if (entity == null) return null;
        return OrderDTO.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .value(entity.getValue())
                .idUser(entity.getIdUser())
                .idDelivery(entity.getIdDelivery())
                .build();
    }

    public static Order toNewEntity(OrderDTO dto) {
        Objects.requireNonNull(dto, "OrderDTO não pode ser nulo");
        Order e = new Order();
        e.setExternalId(UUID.randomUUID().toString());
        updateMutableFields(dto, e);
        return e;
    }

    public static void updateEntityFromDto(OrderDTO dto, Order entity) {
        Objects.requireNonNull(entity, "Order entity não pode ser nula");
        if (dto == null) return;
        updateMutableFields(dto, entity);
    }

    private static void updateMutableFields(OrderDTO dto, Order e) {
        e.setDescription(safe(dto.description()));
        e.setValue(safe(dto.value()));
        e.setIdUser(dto.idUser());
        e.setIdDelivery(dto.idDelivery());
    }

    private static String safe(String s) {
        return s == null ? null : s.trim();
    }

    private static BigDecimal safe(BigDecimal b) {
        return b;
    }
}
