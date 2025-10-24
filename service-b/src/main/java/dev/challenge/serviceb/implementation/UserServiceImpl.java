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
        log.info("Criando usuário (Service B)... email='{}'", dto != null ? dto.email() : null);
        validate(dto);

        User saved = userRepository.save(UserAdapter.toNewEntity(dto));
        UserDTO out = UserAdapter.toUserDTO(saved);
        log.info("Usuário criado. id={}, externalId={}", saved.getId(), saved.getExternalId());

        if (!Replication.incoming()) {
            replicateCreate(out);
        }

        return out;
    }

    @Override
    @Transactional
    public UserDTO updateUser(String externalId, UserDTO dto) {
        log.info("Atualizando usuário... externalId={}", externalId);
        validate(dto);

        User found = userRepository.findByExternalId(externalId)
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado para update. externalId={}", externalId);
                    return new CustomException(HttpStatus.NOT_FOUND,
                            "Usuário não encontrado (ID: " + externalId + ")");
                });

        UserAdapter.updateEntityFromDto(dto, found);
        User saved = userRepository.save(found);
        UserDTO out = UserAdapter.toUserDTO(saved);

        if (!Replication.incoming()) {
            replicateUpdate(externalId, out);
        }

        log.info("Usuário atualizado. externalId={}", externalId);
        return out;
    }

    @Override
    @Transactional
    public void deleteUser(String externalId) {
        log.info("Removendo usuário... externalId={}", externalId);

        boolean exists = userRepository.existsByExternalId(externalId);
        if (!exists) {
            log.warn("Usuário não encontrado para remoção. externalId={}", externalId);
            throw new CustomException(HttpStatus.NOT_FOUND,
                    "Usuário não encontrado (ID: " + externalId + ")");
        }

        userRepository.deleteByExternalId(externalId);

        if (!Replication.incoming()) {
            replicateDelete(externalId);
        }

        log.info("Usuário removido. externalId={}", externalId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> listUsers() {
        log.info("Listando usuários (Service B)...");
        List<UserDTO> list = userRepository.findAll().stream()
                .map(UserAdapter::toUserDTO)
                .toList();
        log.info("Listagem concluída. total={}", list.size());
        return list;
    }

    private void replicateCreate(UserDTO out) {
        try {
            userAClient.createUser(out);
            log.info("Replicação create enviada ao Service A. externalId={}", out.externalId());
        } catch (Exception e) {
            log.error("Falha ao replicar usuário (create) ao Service A. externalId={}, err={}",
                    out.externalId(), e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY,
                    "Falha ao replicar usuário para o Service A");
        }
    }

    private void replicateUpdate(String externalId, UserDTO out) {
        try {
            userAClient.updateUser(externalId, out);
            log.info("Replicação update enviada ao Service A. externalId={}", externalId);
        } catch (Exception e) {
            log.error("Falha ao replicar usuário (update) ao Service A. externalId={}, err={}",
                    externalId, e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY,
                    "Falha ao replicar atualização de usuário para o Service A (ID: " + externalId + ")");
        }
    }

    private void replicateDelete(String externalId) {
        try {
            userAClient.deleteUser(externalId);
            log.info("Replicação delete enviada ao Service A. externalId={}", externalId);
        } catch (Exception e) {
            log.error("Falha ao replicar usuário (delete) ao Service A. externalId={}, err={}",
                    externalId, e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY,
                    "Falha ao replicar exclusão de usuário para o Service A (ID: " + externalId + ")");
        }
    }

    private static void validate(UserDTO dto) {
        if (dto == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Payload obrigatório ausente.");
        }
        if (isBlank(dto.name())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Campo 'name' é obrigatório.");
        }
        if (isBlank(dto.email())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Campo 'email' é obrigatório.");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
