package com.example.jackpot.jackpot.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JackpotRewardRepository extends JpaRepository<JackpotRewardEntity, UUID> {
    Optional<JackpotRewardEntity> findByBetId(UUID betId);
}
