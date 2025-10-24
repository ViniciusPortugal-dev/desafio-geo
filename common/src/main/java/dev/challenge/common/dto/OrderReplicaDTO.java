package dev.challenge.common.dto;

import java.math.BigDecimal;

public record OrderReplicaDTO(Long id,
                                 String description,
                                 BigDecimal value,
                                 Long idUser,
                                 String externalId,
                                 String deliveryName,
                                 String deliveryPhone) {}
