package com.example.jackpot.jackpot.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JackpotRewardEvaluationRepository
        extends JpaRepository<JackpotRewardEvaluationEntity, UUID> {
    Optional<JackpotRewardEvaluationEntity> findByBetId(UUID betId);
}
