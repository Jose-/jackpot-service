package com.example.jackpot.bet.application;

import com.example.jackpot.jackpot.domain.BetStatus;
import java.util.UUID;

public record PublishBetResult(UUID betId, BetStatus status) {}
