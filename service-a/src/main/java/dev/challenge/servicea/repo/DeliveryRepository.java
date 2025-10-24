package dev.challenge.servicea.repo;

import dev.challenge.servicea.domain.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {}
