package dev.challenge.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderReplicaDTO(Long id,
                              String description,
                              @NotNull(message = "value is required")
                              BigDecimal value,
                              String externalId,
                              @NotBlank(message = "deliveryName is required")
                              String deliveryName,
                              @NotBlank(message = "deliveryPhone is required")
                              String deliveryPhone,
                              @NotBlank(message = "externalUserId is required")
                              String externalUserId) {
}
