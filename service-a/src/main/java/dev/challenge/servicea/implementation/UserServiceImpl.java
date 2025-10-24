package dev.challenge.servicea.implementation;

import dev.challenge.common.dto.UserDTO;
import dev.challenge.common.error.CustomException;
import dev.challenge.common.replication.Replication;
import dev.challenge.servicea.adapters.UserAdapter;
import dev.challenge.servicea.domain.User;
import dev.challenge.servicea.feign.UserBClient;
import dev.challenge.servicea.replication.Force422;
import dev.challenge.servicea.repo.UsuarioRepository;
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
public class UserServiceImpl implements UserService {

    private final UsuarioRepository usuarioRepository;
    private final UserBClient client;
    private final Force422 force422;

    @Override
    @Transactional
    public UserDTO createUser(UserDTO dto) {
        log.info("Criando usuário... email='{}'", dto != null ? dto.email() : null);
        validate(dto);
        User saved = usuarioRepository.save(UserAdapter.toNewEntity(dto));
        UserDTO out = UserAdapter.toUserDTO(saved);
        log.info("Usuário criado. id={}, externalId={}", saved.getId(), saved.getExternalId());
        force422.registerLocalCreateSuccess();
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
        User found = usuarioRepository.findByExternalId(externalId)
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado para update. externalId={}", externalId);
                    return new CustomException(HttpStatus.NOT_FOUND,
                            "Usuário não encontrado (ID: " + externalId + ")");
                });

        UserAdapter.updateEntityFromDto(dto, found);
        User saved = usuarioRepository.save(found);
        UserDTO out = UserAdapter.toUserDTO(saved);

        if (!Replication.incoming()) {
            replicateUpdate(externalId, out);
        }

        log.info("Usuário atualizado com sucesso. externalId={}", externalId);
        return out;
    }

    @Override
    @Transactional
    public void deleteUser(String externalId) {
        log.info("Removendo usuário... externalId={}", externalId);

        boolean exists = usuarioRepository.existsByExternalId(externalId);
        if (!exists) {
            log.warn("Usuário não encontrado para remoção. externalId={}", externalId);
            throw new CustomException(HttpStatus.NOT_FOUND, "Usuário não encontrado (ID: " + externalId + ")");
        }

        usuarioRepository.deleteByExternalId(externalId);

        if (!Replication.incoming()) {
            replicateDelete(externalId);
        }

        log.info("Usuário removido. externalId={}", externalId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> listUsers() {
        log.info("Listando usuários...");
        List<UserDTO> list = usuarioRepository.findAll().stream()
                .map(UserAdapter::toUserDTO)
                .toList();
        log.info("Listagem concluída. total={}", list.size());
        return list;
    }

    private void replicateCreate(UserDTO out) {
        try {
            String forceHeader = force422.shouldForceNext422AndReset() ? "true" : null;
            client.createUser(out, forceHeader);
            log.info("Replicação create enviada ao Service B. externalId={}", out.externalId());
        } catch (Exception e) {
            log.error("Falha ao replicar usuário (create). externalId={}, err={}",
                    out.externalId(), e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY,
                    "Falha ao replicar usuário para o Service B");
        }
    }

    private void replicateUpdate(String externalId, UserDTO out) {
        try {
            client.updateUser(externalId, out);
            log.info("Replicação update enviada ao Service B. externalId={}", externalId);
        } catch (Exception e) {
            log.error("Falha ao replicar usuário (update). externalId={}, err={}",
                    externalId, e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY,
                    "Falha ao replicar atualização de usuário para o Service B (ID: " + externalId + ")");
        }
    }

    private void replicateDelete(String externalId) {
        try {
            client.deleteUser(externalId);
            log.info("Replicação delete enviada ao Service B. externalId={}", externalId);
        } catch (Exception e) {
            log.error("Falha ao replicar usuário (delete). externalId={}, err={}",
                    externalId, e.getMessage(), e);
            throw new CustomException(HttpStatus.BAD_GATEWAY,
                    "Falha ao replicar exclusão de usuário para o Service B (ID: " + externalId + ")");
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
