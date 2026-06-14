package com.example.jackpot.jackpot.domain;

import com.example.jackpot.shared.domain.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Bet {

    private final UUID id;
    private final UUID userId;
    private final UUID jackpotId;
    private final BigDecimal amount;
    private final Instant createdAt;
    private BetStatus status;
    private Instant updatedAt;

    public Bet(UUID id, UUID userId, UUID jackpotId, BigDecimal amount, Instant now) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.jackpotId = Objects.requireNonNull(jackpotId, "jackpotId must not be null");
        this.amount = new Money(amount).value();
        this.createdAt = Objects.requireNonNull(now, "now must not be null");
        this.updatedAt = now;
        this.status = BetStatus.PENDING_PUBLICATION;
    }

    public UUID id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public UUID jackpotId() {
        return jackpotId;
    }

    public BigDecimal amount() {
        return amount;
    }

    public BetStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
