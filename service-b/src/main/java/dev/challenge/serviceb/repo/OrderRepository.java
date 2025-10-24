package dev.challenge.serviceb.repo;

import dev.challenge.serviceb.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByExternalId(String id);
    boolean existsByExternalId(String id);
    void deleteByExternalId(String id);
}
