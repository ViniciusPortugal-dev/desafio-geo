package dev.challenge.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderDTO(Long id,
                       String description,
                       BigDecimal value,
                       Long idDelivery,
                       String externalId,
                       String externalUserId) {
}
