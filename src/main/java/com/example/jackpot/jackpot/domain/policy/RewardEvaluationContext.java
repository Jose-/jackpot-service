package com.example.jackpot.jackpot.domain.policy;

import com.example.jackpot.jackpot.domain.Jackpot;
import java.math.BigDecimal;
import java.util.Objects;

public record RewardEvaluationContext(Jackpot jackpot, BigDecimal betAmount, BigDecimal draw) {

    public RewardEvaluationContext {
        Objects.requireNonNull(jackpot, "jackpot must not be null");
        Objects.requireNonNull(betAmount, "betAmount must not be null");
        Objects.requireNonNull(draw, "draw must not be null");
    }
}
