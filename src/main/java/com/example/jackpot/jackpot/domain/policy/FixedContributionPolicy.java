package com.example.jackpot.jackpot.domain.policy;

import com.example.jackpot.jackpot.domain.Jackpot;
import com.example.jackpot.jackpot.domain.configuration.FixedContributionConfiguration;
import com.example.jackpot.shared.domain.MonetaryPrecision;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class FixedContributionPolicy implements ContributionPolicy {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    @Override
    public String strategy() {
        return FixedContributionConfiguration.STRATEGY;
    }

    @Override
    public BigDecimal calculate(BigDecimal stake, Jackpot jackpot) {
        stake = Objects.requireNonNull(stake, "stake must not be null");
        Objects.requireNonNull(jackpot, "jackpot must not be null");

        if (stake.signum() < 0) {
            throw new IllegalArgumentException("Stake must be non-negative");
        }
        if (!strategy().equals(jackpot.contributionConfiguration().strategy())) {
            throw new IllegalArgumentException("Jackpot must use fixed contribution configuration");
        }
        var ratePercentage = jackpot.contributionConfiguration().parameters().get("percentage");

        return stake.multiply(
                        Objects.requireNonNull(
                                ratePercentage, "rate percentage parameter must not be null"))
                .divide(ONE_HUNDRED, MonetaryPrecision.LEDGER_SCALE, RoundingMode.HALF_UP);
    }
}
