package com.example.jackpot.jackpot.domain.policy;

import com.example.jackpot.jackpot.domain.RewardDecision;
import com.example.jackpot.jackpot.domain.configuration.FixedRewardConfiguration;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public final class FixedRewardPolicy implements RewardPolicy {

    @Override
    public String strategy() {
        return FixedRewardConfiguration.STRATEGY;
    }

    @Override
    public RewardDecision evaluate(RewardEvaluationContext context) {
        BigDecimal winProbabilityPercentage =
                new FixedRewardConfiguration(
                                context.jackpot().rewardConfiguration().parameters().get("chance"))
                        .winProbabilityPercentage();
        return new RewardDecision(
                context.draw().compareTo(winProbabilityPercentage) < 0,
                winProbabilityPercentage,
                context.draw());
    }
}
