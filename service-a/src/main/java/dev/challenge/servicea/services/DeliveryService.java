package dev.challenge.servicea.services;

import dev.challenge.common.dto.DeliveryDTO;

import java.util.List;

public interface DeliveryService {
    DeliveryDTO createDelivery(DeliveryDTO dto);
    DeliveryDTO updateDelivery(Long id, DeliveryDTO dto);
    void deleteDelivery(Long id);
    List<DeliveryDTO> listDeliveries();
}
