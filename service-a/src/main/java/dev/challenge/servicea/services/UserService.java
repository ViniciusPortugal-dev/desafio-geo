package dev.challenge.servicea.services;

import dev.challenge.common.dto.UserDTO;

import java.util.List;
public interface UserService {

    UserDTO createUser(UserDTO dto);

    UserDTO updateUser(String id, UserDTO dto);

    void deleteUser(String id);

    List<UserDTO> listUsers();
}
