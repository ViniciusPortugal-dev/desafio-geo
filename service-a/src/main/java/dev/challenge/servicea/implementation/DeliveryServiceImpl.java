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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;

    @Override
    @Transactional
    public DeliveryDTO createDelivery(DeliveryDTO dto) {
        log.info("Creating delivery... name='{}'", dto.name());
        try {
            Delivery saved = deliveryRepository.save(DeliveryAdapter.toNewEntity(dto));
            log.info("Delivery created. id={}", saved.getId());
            return DeliveryAdapter.toDeliveryDTO(saved);
        } catch (Exception e) {
            log.error("Error creating delivery. payload=name='{}'", dto.name(), e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating delivery");
        }
    }

    @Override
    @Transactional
    public DeliveryDTO updateDelivery(Long id, DeliveryDTO dto) {
        log.info("Updating delivery... id={}, name='{}'", id, dto.name());

        Delivery entity = deliveryRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Delivery not found (ID: " + id + ")"));

        try {
            DeliveryAdapter.updateEntityFromDto(dto, entity);
            Delivery saved = deliveryRepository.save(entity);
            log.info("Delivery updated. id={}", saved.getId());
            return DeliveryAdapter.toDeliveryDTO(saved);
        } catch (Exception e) {
            log.error("Error updating delivery. id={}", id, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating delivery");
        }
    }

    @Override
    @Transactional
    public void deleteDelivery(Long id) {
        log.info("Deleting delivery... id={}", id);
        if (!deliveryRepository.existsById(id)) {
            throw new CustomException(HttpStatus.NOT_FOUND, "Delivery not found (ID: " + id + ")");
        }

        try {
            deliveryRepository.deleteById(id);
            log.info("Delivery deleted. id={}", id);
        } catch (Exception e) {
            log.error("Error deleting delivery. id={}", id, e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting delivery");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryDTO> listDeliveries() {
        log.info("Listing deliveries...");
        List<DeliveryDTO> result;
        try {
            result = deliveryRepository.findAll()
                    .stream()
                    .map(DeliveryAdapter::toDeliveryDTO)
                    .toList();
            if (result.isEmpty()) {
                log.warn("No deliveries found.");
                throw new CustomException(HttpStatus.NO_CONTENT, "No deliveries found.");
            }
            log.info("Listing completed. total={}", result.size());
            return result;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error listing deliveries.", e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error listing deliveries");
        }
    }

}


