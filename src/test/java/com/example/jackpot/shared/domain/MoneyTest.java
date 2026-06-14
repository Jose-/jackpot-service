package com.example.jackpot.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Money")
class MoneyTest {
    @Test
    @DisplayName("Should normalize valid monetary values to two decimal places")
    void shouldNormalizeValidMonetaryValuesToTwoDecimalPlaces() {
        assertThat(new Money(new BigDecimal("10")).value()).isEqualTo(new BigDecimal("10.00"));
        assertThat(new Money(new BigDecimal("10.0")).value()).isEqualTo(new BigDecimal("10.00"));
        assertThat(new Money(new BigDecimal("0.01")).value()).isEqualTo(new BigDecimal("0.01"));
    }

    @Test
    @DisplayName("Should reject values with unsupported fractional precision")
    void shouldRejectValuesWithUnsupportedFractionalPrecision() {
        assertThatThrownBy(() -> new Money(new BigDecimal("10.005")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("two decimal");
    }

    @Test
    @DisplayName("Should reject non-positive and oversized values")
    void shouldRejectNonPositiveAndOversizedValues() {
        assertThatThrownBy(() -> new Money(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Money(new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Money(new BigDecimal("100000000000000000.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("precision");
    }
}
