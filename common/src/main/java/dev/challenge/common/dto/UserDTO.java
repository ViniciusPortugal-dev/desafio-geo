package dev.challenge.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserDTO(Long id,
                      @NotBlank(message = "name is required") String name,
                      @NotBlank(message = "email is required")
                      @Email(message = "email must be valid")
                      String email,
                      String externalId) {}
