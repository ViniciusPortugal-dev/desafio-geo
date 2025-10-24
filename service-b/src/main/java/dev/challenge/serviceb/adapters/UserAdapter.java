package dev.challenge.serviceb.adapters;

import dev.challenge.common.dto.UserDTO;
import dev.challenge.serviceb.domain.User;

import java.util.Objects;

public final class UserAdapter {

    private UserAdapter() {}

    public static UserDTO toUserDTO(User e) {
        if (e == null) return null;
        return UserDTO.builder()
                .id(e.getId())
                .name(e.getName())
                .email(e.getEmail())
                .externalId(e.getExternalId())
                .build();
    }

    public static User toNewEntity(UserDTO dto) {
        Objects.requireNonNull(dto, "UserDTO não pode ser nulo");
        User e = new User();
        e.setName(safe(dto.name()));
        e.setEmail(safe(dto.email()));
        e.setExternalId(dto.externalId()); // vem do A
        return e;
    }

    public static void updateEntityFromDto(UserDTO dto, User e) {
        Objects.requireNonNull(e, "User entity não pode ser nulo");
        if (dto == null) return;
        e.setName(safe(dto.name()));
        e.setEmail(safe(dto.email()));
    }

    private static String safe(String s) {
        return s == null ? null : s.trim();
    }
}
