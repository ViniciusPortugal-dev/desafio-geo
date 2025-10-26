package dev.challenge.serviceb.implementation;

import dev.challenge.common.dto.UserDTO;
import dev.challenge.common.error.CustomException;
import dev.challenge.common.replication.Replication;
import dev.challenge.serviceb.adapters.UserAdapter;
import dev.challenge.serviceb.domain.User;
import dev.challenge.serviceb.feign.UserAClient;
import dev.challenge.serviceb.repo.UserRepository;
import dev.challenge.serviceb.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserAClient userAClient;

    @Override
    @Transactional
    public UserDTO createUser(UserDTO dto) {
        try {
            User savedEntity = userRepository.save(UserAdapter.toNewEntity(dto));
            UserDTO out = UserAdapter.toUserDTO(savedEntity);

            log.info("User created. id={}, externalId={}", savedEntity.getId(), savedEntity.getExternalId());

            if (!Replication.incoming()) {
                replicateCreate(out);
            }
            return out;
        } catch (Exception e) {
            log.error("Error creating user (Service B). payload={}", dto, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating user");
        }
    }

    @Override
    @Transactional
    public UserDTO updateUser(String externalId, UserDTO dto) {
        log.info("Updating user (Service B)... externalId={}", externalId);

        User foundEntity = userRepository.findByExternalId(externalId)
                .orElseThrow(() -> {
                    log.warn("User not found for update (Service B). externalId={}", externalId);
                    return new CustomException(HttpStatus.NOT_FOUND, "User not found (externalId: " + externalId + ")");
                });

        try {
            UserAdapter.updateEntityFromDto(dto, foundEntity);
            User savedEntity = userRepository.save(foundEntity);
            UserDTO out = UserAdapter.toUserDTO(savedEntity);

            if (!Replication.incoming()) {
                replicateUpdate(externalId, out);
            }

            log.info("User updated (Service B). externalId={}", externalId);
            return out;
        } catch (Exception e) {
            log.error("Error updating user (Service B). externalId={}, payload={}", externalId, dto, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating user");
        }
    }

    @Override
    @Transactional
    public void deleteUser(String externalId) {
        log.info("Deleting user (Service B)... externalId={}", externalId);
        boolean exists = userRepository.existsByExternalId(externalId);
        if (!exists) {
            log.warn("User not found for deletion (Service B). externalId={}", externalId);
            throw new CustomException(HttpStatus.NOT_FOUND, "User not found (externalId: " + externalId + ")");
        }

        try {
            userRepository.deleteByExternalId(externalId);

            if (!Replication.incoming()) {
                replicateDelete(externalId);
            }

            log.info("User deleted (Service B). externalId={}", externalId);
        } catch (Exception e) {
            log.error("Error deleting user (Service B). externalId={}", externalId, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting user");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> listUsers() {
        log.info("Listing users (Service B)...");
        try {
            List<UserDTO> result = userRepository.findAll().stream()
                    .map(UserAdapter::toUserDTO)
                    .toList();

            if (result.isEmpty()) {
                log.warn("No users found (Service B).");
                throw new CustomException(HttpStatus.NO_CONTENT, "No users found.");
            }

            log.info("Listing completed. total={}", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error listing users (Service B).", e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error listing users");
        }
    }

    private void replicateCreate(UserDTO out) {
        try {
            userAClient.createUser(out);
            log.info("Create replication sent to Service A. externalId={}", out.externalId());
        } catch (Exception e) {
            log.error("Failed to replicate user (create) to Service A. externalId={}, err={}",
                    out.externalId(), e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY, "Failed to replicate user to Service A");
        }
    }

    private void replicateUpdate(String externalId, UserDTO out) {
        try {
            userAClient.updateUser(externalId, out);
            log.info("Update replication sent to Service A. externalId={}", externalId);
        } catch (Exception e) {
            log.error("Failed to replicate user (update) to Service A. externalId={}, err={}",
                    externalId, e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY,
                    "Failed to replicate user update to Service A (externalId: " + externalId + ")");
        }
    }

    private void replicateDelete(String externalId) {
        try {
            userAClient.deleteUser(externalId);
            log.info("Delete replication sent to Service A. externalId={}", externalId);
        } catch (Exception e) {
            log.error("Failed to replicate user (delete) to Service A. externalId={}, err={}",
                    externalId, e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY,
                    "Failed to replicate user deletion to Service A (externalId: " + externalId + ")");
        }
    }
}
