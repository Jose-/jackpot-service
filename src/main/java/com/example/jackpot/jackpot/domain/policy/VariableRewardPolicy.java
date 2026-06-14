package com.example.jackpot.jackpot.domain.policy;

import com.example.jackpot.jackpot.domain.Jackpot;
import com.example.jackpot.jackpot.domain.RewardDecision;
import com.example.jackpot.jackpot.domain.configuration.VariableRewardConfiguration;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public final class VariableRewardPolicy implements RewardPolicy {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    @Override
    public String strategy() {
        return VariableRewardConfiguration.STRATEGY;
    }

    @Override
    public RewardDecision evaluate(RewardEvaluationContext context) {
        Jackpot jackpot = context.jackpot();
        var parameters = jackpot.rewardConfiguration().parameters();
        var configuration =
                new VariableRewardConfiguration(
                        parameters.get("initialWinProbabilityPercentage"),
                        parameters.get("winProbabilityIncreasePerIntervalPercentagePoints"),
                        parameters.get("poolGrowthIntervalAmount"),
                        parameters.get("guaranteedWinPoolAmount"));
        configuration.validateFor(jackpot.initialPool());

        BigDecimal winProbabilityPercentage;
        if (jackpot.currentPool().compareTo(configuration.guaranteedWinPoolAmount()) >= 0) {
            winProbabilityPercentage = ONE_HUNDRED;
        } else {
            BigDecimal poolGrowth =
                    jackpot.currentPool().subtract(jackpot.initialPool()).max(BigDecimal.ZERO);
            BigDecimal completedIntervals =
                    poolGrowth.divideToIntegralValue(configuration.poolGrowthIntervalAmount());
            winProbabilityPercentage =
                    configuration
                            .initialWinProbabilityPercentage()
                            .add(
                                    configuration
                                            .winProbabilityIncreasePerIntervalPercentagePoints()
                                            .multiply(completedIntervals))
                            .min(ONE_HUNDRED);
        }
        return new RewardDecision(
                context.draw().compareTo(winProbabilityPercentage) < 0,
                winProbabilityPercentage,
                context.draw());
    }
}
