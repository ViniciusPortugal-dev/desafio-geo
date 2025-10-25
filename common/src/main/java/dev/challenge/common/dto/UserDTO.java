package dev.challenge.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserDTO(Long id, String name, String email, String externalId) {}
