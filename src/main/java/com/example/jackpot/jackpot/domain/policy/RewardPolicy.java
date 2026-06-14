package com.example.jackpot.jackpot.domain.policy;

import com.example.jackpot.jackpot.domain.Jackpot;
import com.example.jackpot.jackpot.domain.RewardDecision;
import java.math.BigDecimal;

public interface RewardPolicy {

    String strategy();

    RewardDecision evaluate(RewardEvaluationContext context);

    default RewardDecision evaluate(Jackpot jackpot, BigDecimal betAmount, BigDecimal draw) {
        return evaluate(new RewardEvaluationContext(jackpot, betAmount, draw));
    }
}
