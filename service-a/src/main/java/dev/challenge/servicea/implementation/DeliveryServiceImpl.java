package dev.challenge.servicea.implementation;

import dev.challenge.common.dto.DeliveryDTO;
import dev.challenge.common.error.CustomException;
import dev.challenge.servicea.adapters.DeliveryAdapter;
import dev.challenge.servicea.domain.Delivery;
import dev.challenge.servicea.repo.DeliveryRepository;
import dev.challenge.servicea.services.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;

    @Override
    @Transactional
    public DeliveryDTO createDelivery(DeliveryDTO dto) {
        log.info("Criando entregador... payload=name='{}'", safe(dto == null ? null : dto.name()));
        validateRequired(dto);
        try {
            Delivery entity = DeliveryAdapter.toNewEntity(dto);
            Delivery saved = deliveryRepository.save(entity);
            log.info("Entregador criado com sucesso. id={}", saved.getId());
            return DeliveryAdapter.toDeliveryDTO(saved);
        } catch (Exception e) {
            log.error("Erro ao criar entregador. payload={}", dto, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao criar entregador: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public DeliveryDTO updateDelivery(Long id, DeliveryDTO dto) {
        log.info("Atualizando entregador... id={}, payload=name='{}'", id, safe(dto == null ? null : dto.name()));
        validateRequired(dto);
        Delivery entity = deliveryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Entregador não encontrado para update. id={}", id);
                    return new CustomException(HttpStatus.NOT_FOUND, "Entregador não encontrado (ID: " + id + ")");
                });

        try {
            DeliveryAdapter.updateEntityFromDto(dto, entity);
            Delivery saved = deliveryRepository.save(entity);
            log.info("Entregador atualizado com sucesso. id={}", saved.getId());
            return DeliveryAdapter.toDeliveryDTO(saved);
        } catch (Exception e) {
            log.error("Erro ao atualizar entregador. id={}, payload={}", id, dto, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao atualizar entregador: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteDelivery(Long id) {
        log.info("Removendo entregador... id={}", id);

        boolean exists = deliveryRepository.existsById(id);
        if (!exists) {
            log.warn("Entregador não encontrado para remoção. id={}", id);
            throw new CustomException(HttpStatus.NOT_FOUND, "Entregador não encontrado (ID: " + id + ")");
        }

        try {
            deliveryRepository.deleteById(id);
            log.info("Entregador removido com sucesso. id={}", id);
        } catch (Exception e) {
            log.error("Erro ao remover entregador. id={}", id, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao remover entregador: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryDTO> listDeliveries() {
        log.info("Listando entregadores...");
        try {
            List<DeliveryDTO> result = deliveryRepository.findAll()
                    .stream()
                    .map(DeliveryAdapter::toDeliveryDTO)
                    .toList();

            if (result.isEmpty()) {
                log.warn("Nenhum entregador encontrado na listagem.");
                throw new CustomException(HttpStatus.NO_CONTENT, "Nenhum entregador encontrado.");
            }

            log.info("Listagem concluída. total={}", result.size());
            return result;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao listar entregadores.", e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao listar entregadores: " + e.getMessage());
        }
    }

    private static void validateRequired(DeliveryDTO dto) {
        if (dto == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Payload obrigatório ausente.");
        }
        if (isBlank(dto.name())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Campo 'name' é obrigatório.");
        }
        if (isBlank(dto.phone())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Campo 'phone' é obrigatório.");
        }
    }

    private static String safe(String s) {
        return s == null ? null : s.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
