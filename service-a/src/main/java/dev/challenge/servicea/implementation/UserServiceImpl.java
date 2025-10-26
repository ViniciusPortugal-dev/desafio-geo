package dev.challenge.servicea.implementation;

import dev.challenge.common.dto.UserDTO;
import dev.challenge.common.error.CustomException;
import dev.challenge.common.replication.Replication;
import dev.challenge.servicea.adapters.UserAdapter;
import dev.challenge.servicea.domain.User;
import dev.challenge.servicea.feign.UserBClient;
import dev.challenge.servicea.replication.Force422;
import dev.challenge.servicea.repo.UserRepository;
import dev.challenge.servicea.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserBClient client;
    private final Force422 force422;

    @Override
    @Transactional
    public UserDTO createUser(UserDTO dto) {
        try {
            User saved = userRepository.save(UserAdapter.toNewEntity(dto));
            UserDTO out = UserAdapter.toUserDTO(saved);

            log.info("User created. id={}, externalId={}", saved.getId(), saved.getExternalId());

            if (!Replication.incoming()) {
                force422.registerLocalCreateSuccess();
                if (force422.shouldForceNext422AndReset()) {
                    throw new CustomException(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Intentional exception for evaluation (case 5.1)");
                }
                replicateCreate(out);
            }
            return out;
        } catch (Exception e) {
            log.error("Error creating user. payload={}", dto, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating user");
        }
    }

    @Override
    @Transactional
    public UserDTO updateUser(String externalId, UserDTO dto) {
        log.info("Updating user... externalId={}", externalId);

        User found = userRepository.findByExternalId(externalId)
                .orElseThrow(() -> {
                    log.warn("User not found for update. externalId={}", externalId);
                    return new CustomException(HttpStatus.NOT_FOUND,
                            "User not found (externalId: " + externalId + ")");
                });

        try {
            UserAdapter.updateEntityFromDto(dto, found);
            User saved = userRepository.save(found);
            UserDTO out = UserAdapter.toUserDTO(saved);

            if (!Replication.incoming()) {
                replicateUpdate(externalId, out);
            }

            log.info("User updated. externalId={}", externalId);
            return out;
        } catch (Exception e) {
            log.error("Error updating user. externalId={}, payload={}", externalId, dto, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating user");
        }
    }

    @Override
    @Transactional
    public void deleteUser(String externalId) {
        log.info("Deleting user... externalId={}", externalId);

        boolean exists = userRepository.existsByExternalId(externalId);
        if (!exists) {
            log.warn("User not found for deletion. externalId={}", externalId);
            throw new CustomException(HttpStatus.NOT_FOUND,
                    "User not found (externalId: " + externalId + ")");
        }

        try {
            userRepository.deleteByExternalId(externalId);

            if (!Replication.incoming()) {
                replicateDelete(externalId);
            }

            log.info("User deleted. externalId={}", externalId);
        } catch (Exception e) {
            log.error("Error deleting user. externalId={}", externalId, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting user");
        }
    }

    @Override
    public List<UserDTO> listUsers() {
        log.info("Listing users...");
        try {
            List<UserDTO> list = userRepository.findAll().stream()
                    .map(UserAdapter::toUserDTO)
                    .toList();

            if (list.isEmpty()) {
                log.warn("No users found.");
                throw new CustomException(HttpStatus.NO_CONTENT, "No users found.");
            }

            log.info("Listing completed. total={}", list.size());
            return list;
        } catch (Exception e) {
            log.error("Error listing users.", e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error listing users");
        }
    }

    private void replicateCreate(UserDTO out) {
        try {
            client.createUser(out);
            log.info("Create replication sent to Service B. externalId={}", out.externalId());
        } catch (Exception e) {
            log.error("Failed to replicate user (create). externalId={}, err={}",
                    out.externalId(), e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY,
                    "Failed to replicate user to Service B");
        }
    }

    private void replicateUpdate(String externalId, UserDTO out) {
        try {
            client.updateUser(externalId, out);
            log.info("Update replication sent to Service B. externalId={}", externalId);
        } catch (Exception e) {
            log.error("Failed to replicate user (update). externalId={}, err={}",
                    externalId, e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY,
                    "Failed to replicate user update to Service B (externalId: " + externalId + ")");
        }
    }

    private void replicateDelete(String externalId) {
        try {
            client.deleteUser(externalId);
            log.info("Delete replication sent to Service B. externalId={}", externalId);
        } catch (Exception e) {
            log.error("Failed to replicate user (delete). externalId={}, err={}",
                    externalId, e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY,
                    "Failed to replicate user deletion to Service B (externalId: " + externalId + ")");
        }
    }
}
