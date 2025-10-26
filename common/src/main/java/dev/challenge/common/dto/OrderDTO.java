package dev.challenge.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderDTO(Long id,
                       @NotBlank(message = "description is required")
                       String description,
                       @NotNull(message = "value is required")
                       BigDecimal value,
                       @NotNull(message = "idDelivery is required")
                       Long idDelivery,
                       String externalId,
                       @NotBlank(message = "externalUserId is required")
                       String externalUserId) {
}
