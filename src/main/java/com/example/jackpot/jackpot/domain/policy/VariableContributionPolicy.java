package com.example.jackpot.jackpot.domain.policy;

import com.example.jackpot.jackpot.domain.Jackpot;
import com.example.jackpot.jackpot.domain.configuration.VariableContributionConfiguration;
import com.example.jackpot.shared.domain.MonetaryPrecision;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class VariableContributionPolicy implements ContributionPolicy {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    @Override
    public String strategy() {
        return VariableContributionConfiguration.STRATEGY;
    }

    @Override
    public BigDecimal calculate(BigDecimal stake, Jackpot jackpot) {
        stake = Objects.requireNonNull(stake, "stake must not be null");
        Objects.requireNonNull(jackpot, "jackpot must not be null");

        if (stake.signum() < 0) {
            throw new IllegalArgumentException("Stake must be non-negative");
        }
        if (!strategy().equals(jackpot.contributionConfiguration().strategy())) {
            throw new IllegalArgumentException(
                    "Jackpot must use variable contribution configuration");
        }
        var parameters = jackpot.contributionConfiguration().parameters();
        var configuration =
                new VariableContributionConfiguration(
                        parameters.get("initialRatePercentage"),
                        parameters.get("rateDecreasePerIntervalPercentagePoints"),
                        parameters.get("poolGrowthIntervalAmount"),
                        parameters.get("minimumRatePercentage"));

        BigDecimal poolGrowth =
                jackpot.currentPool().subtract(jackpot.initialPool()).max(BigDecimal.ZERO);
        BigDecimal completedIntervals =
                poolGrowth.divideToIntegralValue(configuration.poolGrowthIntervalAmount());
        BigDecimal rateReduction =
                configuration
                        .rateDecreasePerIntervalPercentagePoints()
                        .multiply(completedIntervals);
        BigDecimal appliedRatePercentage =
                configuration
                        .initialRatePercentage()
                        .subtract(rateReduction)
                        .max(configuration.minimumRatePercentage());

        return stake.multiply(appliedRatePercentage)
                .divide(ONE_HUNDRED, MonetaryPrecision.LEDGER_SCALE, RoundingMode.HALF_UP);
    }
}
