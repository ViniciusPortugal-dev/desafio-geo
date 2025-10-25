package dev.challenge.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record DeliveryDTO(Long id, String name, String phone) {}
