package dev.challenge.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record DeliveryDTO(Long id, @NotBlank String name, @NotBlank String phone) {}
