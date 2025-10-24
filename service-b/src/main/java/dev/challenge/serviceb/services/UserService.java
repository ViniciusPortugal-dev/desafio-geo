package dev.challenge.serviceb.services;

import dev.challenge.common.dto.UserDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService {

    UserDTO createUser(UserDTO dto);

    UserDTO updateUser(String id, UserDTO dto);

    void deleteUser(String id);

    List<UserDTO> listUsers();
}
