package com.example.jackpot.bet.persistence;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface BetRepository extends JpaRepository<BetEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BetEntity b where b.id = :id")
    Optional<BetEntity> findByIdForUpdate(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            """
            select b from BetEntity b
            where b.status = com.example.jackpot.jackpot.domain.BetStatus.PENDING_PUBLICATION
            and b.updatedAt < :before
            order by b.updatedAt
            """)
    List<BetEntity> findRecoverableForUpdate(Instant before, Pageable pageable);
}
