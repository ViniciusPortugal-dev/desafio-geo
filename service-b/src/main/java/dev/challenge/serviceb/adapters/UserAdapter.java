package dev.challenge.serviceb.adapters;

import dev.challenge.common.dto.UserDTO;
import dev.challenge.serviceb.domain.User;

import java.util.UUID;

public final class UserAdapter {

    private UserAdapter() {
    }

    public static UserDTO toUserDTO(User userEntity) {
        return UserDTO.builder()
                .id(userEntity.getId())
                .name(userEntity.getName())
                .email(userEntity.getEmail())
                .externalId(userEntity.getExternalId())
                .build();
    }

    public static User toNewEntity(UserDTO userDTO) {
        return User.builder()
                .name(userDTO.name())
                .email(userDTO.email())
                .externalId(userDTO.externalId() != null
                        ? userDTO.externalId()
                        : UUID.randomUUID().toString())
                .build();
    }

    public static void updateEntityFromDto(UserDTO userDTO, User userEntity) {
        userEntity.setName(userDTO.name());
        userEntity.setEmail(userDTO.email());
    }
}


