package dev.challenge.servicea.adapters;

import dev.challenge.common.dto.UserDTO;
import dev.challenge.servicea.domain.User;

import java.util.UUID;

public final class UserAdapter {

    private UserAdapter() {}

    public static UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .externalId(user.getExternalId())
                .build();
    }

    public static User toNewEntity(UserDTO dto) {
        return User.builder()
                .name(dto.name())
                .email(dto.email())
                .externalId(dto.externalId() != null ? dto.externalId() : UUID.randomUUID().toString())
                .build();
    }


    public static void updateEntityFromDto(UserDTO dto, User entity) {
        entity.setName(dto.name());
        entity.setEmail(dto.email());
    }

}
