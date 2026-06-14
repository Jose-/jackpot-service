package com.example.jackpot.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal value) {
    public Money {
        Objects.requireNonNull(value, "Money must not be null");
        if (value.signum() <= 0) {
            throw new IllegalArgumentException("Money must be greater than zero");
        }
        if (value.scale() > MonetaryPrecision.STAKE_SCALE) {
            throw new IllegalArgumentException("Money must have at most two decimal places");
        }

        value = value.setScale(MonetaryPrecision.STAKE_SCALE, RoundingMode.UNNECESSARY);
        if (value.precision() > MonetaryPrecision.STAKE_PRECISION) {
            throw new IllegalArgumentException("Money exceeds supported precision");
        }
    }
}
