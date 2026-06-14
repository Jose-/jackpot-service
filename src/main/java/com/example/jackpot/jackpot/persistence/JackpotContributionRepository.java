package com.example.jackpot.jackpot.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JackpotContributionRepository
        extends JpaRepository<JackpotContributionEntity, UUID> {
    Optional<JackpotContributionEntity> findByBetId(UUID betId);
}
