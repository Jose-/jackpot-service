package com.example.jackpot.jackpot.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface JackpotRepository extends JpaRepository<JackpotEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select j from JackpotEntity j where j.id = :id")
    Optional<JackpotEntity> findByIdForUpdate(UUID id);
}
