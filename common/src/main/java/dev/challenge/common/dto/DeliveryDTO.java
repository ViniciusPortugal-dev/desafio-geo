package dev.challenge.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record DeliveryDTO(Long id,  @NotBlank(message = "name is required") String name, @NotBlank(message = "phone is required") String phone) {}
