package dev.challenge.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserDTO(Long id, @NotBlank String name, @Email String email, String externalId) {}
