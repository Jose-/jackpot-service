package com.example.jackpot.bet.persistence;

import com.example.jackpot.jackpot.domain.BetStatus;
import com.example.jackpot.shared.domain.MonetaryPrecision;
import com.example.jackpot.shared.domain.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "bets")
public class BetEntity {
    @Id private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID jackpotId;

    @Column(
            name = "bet_amount",
            precision = MonetaryPrecision.STAKE_PRECISION,
            scale = MonetaryPrecision.STAKE_SCALE,
            nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BetStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(length = 120)
    private String failureCode;

    @Column(length = 500)
    private String failureMessage;

    private Instant failedAt;
    private int publicationAttempts;

    @Column(length = 500)
    private String lastPublicationError;

    protected BetEntity() {}

    public BetEntity(UUID id, UUID userId, UUID jackpotId, BigDecimal amount, Instant now) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.jackpotId = Objects.requireNonNull(jackpotId);
        this.amount = new Money(amount).value();
        this.createdAt = Objects.requireNonNull(now);
        this.updatedAt = now;
        this.status = BetStatus.PENDING_PUBLICATION;
    }

    public boolean hasSamePayload(UUID userId, UUID jackpotId, BigDecimal amount) {
        return this.userId.equals(userId)
                && this.jackpotId.equals(jackpotId)
                && this.amount.compareTo(new Money(amount).value()) == 0;
    }

    public boolean prepareForPublication(Instant now) {
        if (status == BetStatus.PUBLISHED || status == BetStatus.CONTRIBUTED) {
            return false;
        }
        status = BetStatus.PENDING_PUBLICATION;
        updatedAt = now;
        return true;
    }

    public void claimPublicationRecovery(Instant now) {
        if (status != BetStatus.PENDING_PUBLICATION) {
            throw new IllegalStateException("Bet is not recoverable");
        }
        publicationAttempts++;
        updatedAt = now;
    }

    public void markPublicationStatus(BetStatus publicationStatus, Instant now) {
        if (publicationStatus != BetStatus.PUBLISHED
                && publicationStatus != BetStatus.CONTRIBUTED) {
            throw new IllegalArgumentException("Unsupported successful publication status");
        }
        if (status != BetStatus.CONTRIBUTED) {
            status = publicationStatus;
            updatedAt = now;
        }
    }

    public void markPublicationFailed(String failureMessage, Instant now) {
        if (status == BetStatus.PENDING_PUBLICATION) {
            if (publicationAttempts == 0) {
                status = BetStatus.PUBLICATION_FAILED;
            }
            lastPublicationError = failureMessage;
            updatedAt = now;
        }
    }

    public void markProcessingFailed(String failureCode, String failureMessage, Instant now) {

        if (status == BetStatus.CONTRIBUTED) {
            return;
        }

        status = BetStatus.PROCESSING_FAILED;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.failedAt = now;
        updatedAt = now;
    }

    public void markContributed(Instant now) {
        status = BetStatus.CONTRIBUTED;
        updatedAt = now;
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
}
