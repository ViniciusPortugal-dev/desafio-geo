package dev.challenge.common.dto;

import java.math.BigDecimal;

public record OrderReplicaDTO(Long id,
                                 String description,
                                 BigDecimal value,
                                 String externalId,
                                 String deliveryName,
                                 String deliveryPhone,
                              String externalUserId) {}
