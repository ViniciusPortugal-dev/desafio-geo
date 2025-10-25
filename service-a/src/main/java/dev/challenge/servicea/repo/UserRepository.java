package dev.challenge.servicea.repo;

import dev.challenge.servicea.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByExternalId(String externalId);
    Boolean existsByExternalId(String externalId);
    void deleteByExternalId(String externalId);
}
