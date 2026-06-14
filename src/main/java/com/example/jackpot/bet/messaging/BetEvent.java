package com.example.jackpot.bet.messaging;

import com.example.jackpot.bet.application.PublishBetCommand;
import com.example.jackpot.jackpot.application.ProcessBetContributionCommand;
import java.math.BigDecimal;
import java.util.UUID;

public record BetEvent(UUID betId, UUID userId, UUID jackpotId, BigDecimal amount) {
    public static BetEvent from(PublishBetCommand command) {
        return new BetEvent(
                command.betId(), command.userId(), command.jackpotId(), command.amount());
    }

    public ProcessBetContributionCommand toContributionCommand() {
        return new ProcessBetContributionCommand(betId, userId, jackpotId, amount);
    }
}
