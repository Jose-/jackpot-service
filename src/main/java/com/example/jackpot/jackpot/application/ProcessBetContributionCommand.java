package com.example.jackpot.jackpot.application;

import com.example.jackpot.shared.domain.Money;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record ProcessBetContributionCommand(
        UUID betId, UUID userId, UUID jackpotId, BigDecimal amount) {
    public ProcessBetContributionCommand {
        Objects.requireNonNull(betId, "betId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(jackpotId, "jackpotId must not be null");
        amount = new Money(amount).value();
    }
}
