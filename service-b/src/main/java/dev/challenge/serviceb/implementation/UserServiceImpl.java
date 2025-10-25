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
        try {
            User saved = userRepository.save(UserAdapter.toNewEntity(dto));
            UserDTO out = UserAdapter.toUserDTO(saved);
            log.info("Usuário criado. id={}, externalId={}", saved.getId(), saved.getExternalId());
            if (!Replication.incoming()) {
                replicateCreate(out);
            }
            return out;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao criar usuário (Service B). payload={}", dto, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao criar usuário: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public UserDTO updateUser(String externalId, UserDTO dto) {
        log.info("Atualizando usuário (Service B)... externalId={}", externalId);
        validateExternalId(externalId);
        validate(dto);

        User found = userRepository.findByExternalId(externalId)
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado para update (Service B). externalId={}", externalId);
                    return new CustomException(HttpStatus.NOT_FOUND, "Usuário não encontrado (ID: " + externalId + ")");
                });

        try {
            UserAdapter.updateEntityFromDto(dto, found);
            User saved = userRepository.save(found);
            UserDTO out = UserAdapter.toUserDTO(saved);

            if (!Replication.incoming()) {
                replicateUpdate(externalId, out);
            }

            log.info("Usuário atualizado. externalId={}", externalId);
            return out;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao atualizar usuário (Service B). externalId={}, payload={}", externalId, dto, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao atualizar usuário: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteUser(String externalId) {
        log.info("Removendo usuário (Service B)... externalId={}", externalId);
        validateExternalId(externalId);

        boolean exists = userRepository.existsByExternalId(externalId);
        if (!exists) {
            log.warn("Usuário não encontrado para remoção (Service B). externalId={}", externalId);
            throw new CustomException(HttpStatus.NOT_FOUND, "Usuário não encontrado (ID: " + externalId + ")");
        }

        try {
            userRepository.deleteByExternalId(externalId);

            if (!Replication.incoming()) {
                replicateDelete(externalId);
            }

            log.info("Usuário removido. externalId={}", externalId);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao remover usuário (Service B). externalId={}", externalId, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao remover usuário: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> listUsers() {
        log.info("Listando usuários (Service B)...");
        try {
            List<UserDTO> list = userRepository.findAll().stream()
                    .map(UserAdapter::toUserDTO)
                    .toList();

            if (list.isEmpty()) {
                log.warn("Nenhum usuário encontrado na listagem (Service B).");
                throw new CustomException(HttpStatus.NO_CONTENT, "Nenhum usuário encontrado.");
            }

            log.info("Listagem concluída. total={}", list.size());
            return list;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao listar usuários (Service B).", e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao listar usuários: " + e.getMessage());
        }
    }

    private void replicateCreate(UserDTO out) {
        try {
            userAClient.createUser(out);
            log.info("Replicação create enviada ao Service A. externalId={}", out.externalId());
        } catch (Exception e) {
            log.error("Falha ao replicar usuário (create) ao Service A. externalId={}, err={}", out.externalId(), e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY, "Falha ao replicar usuário para o Service A");
        }
    }

    private void replicateUpdate(String externalId, UserDTO out) {
        try {
            userAClient.updateUser(externalId, out);
            log.info("Replicação update enviada ao Service A. externalId={}", externalId);
        } catch (Exception e) {
            log.error("Falha ao replicar usuário (update) ao Service A. externalId={}, err={}", externalId, e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY, "Falha ao replicar atualização de usuário para o Service A (ID: " + externalId + ")");
        }
    }

    private void replicateDelete(String externalId) {
        try {
            userAClient.deleteUser(externalId);
            log.info("Replicação delete enviada ao Service A. externalId={}", externalId);
        } catch (Exception e) {
            log.error("Falha ao replicar usuário (delete) ao Service A. externalId={}, err={}", externalId, e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY, "Falha ao replicar exclusão de usuário para o Service A (ID: " + externalId + ")");
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

    private static void validateExternalId(String externalId) {
        if (isBlank(externalId)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Parâmetro 'externalId' é obrigatório.");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
