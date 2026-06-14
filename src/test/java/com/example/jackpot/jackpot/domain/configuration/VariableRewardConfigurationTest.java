package com.example.jackpot.jackpot.domain.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Variable reward configuration")
class VariableRewardConfigurationTest {

    @Test
    @DisplayName("Should normalize every scalar when the configuration is valid")
    void shouldNormalizeEveryScalarWhenConfigurationIsValid() {
        var configuration =
                new VariableRewardConfiguration(
                        new BigDecimal("1.23454"),
                        new BigDecimal("0.50005"),
                        new BigDecimal("100.005"),
                        new BigDecimal("1000.005"));

        assertThat(configuration.initialWinProbabilityPercentage()).isEqualByComparingTo("1.2345");
        assertThat(configuration.winProbabilityIncreasePerIntervalPercentagePoints())
                .isEqualByComparingTo("0.5001");
        assertThat(configuration.poolGrowthIntervalAmount())
                .isEqualTo(new BigDecimal("100.00500000"));
        assertThat(configuration.guaranteedWinPoolAmount())
                .isEqualTo(new BigDecimal("1000.00500000"));
    }

    @Test
    @DisplayName("Should reject the configuration when a required scalar is null")
    void shouldRejectConfigurationWhenRequiredScalarIsNull() {
        assertThatNullPointerException()
                .isThrownBy(
                        () ->
                                new VariableRewardConfiguration(
                                        null,
                                        new BigDecimal("1"),
                                        new BigDecimal("100"),
                                        new BigDecimal("1000")))
                .withMessageContaining("initial win probability percentage");
        assertThatNullPointerException()
                .isThrownBy(
                        () ->
                                new VariableRewardConfiguration(
                                        new BigDecimal("1"),
                                        null,
                                        new BigDecimal("100"),
                                        new BigDecimal("1000")))
                .withMessageContaining("win probability increase per interval percentage points");
        assertThatNullPointerException()
                .isThrownBy(
                        () ->
                                new VariableRewardConfiguration(
                                        new BigDecimal("1"),
                                        new BigDecimal("1"),
                                        null,
                                        new BigDecimal("1000")))
                .withMessageContaining("pool growth interval amount");
        assertThatNullPointerException()
                .isThrownBy(
                        () ->
                                new VariableRewardConfiguration(
                                        new BigDecimal("1"),
                                        new BigDecimal("1"),
                                        new BigDecimal("100"),
                                        null))
                .withMessageContaining("guaranteed win pool amount");
    }

    @Test
    @DisplayName("Should reject the configuration when a chance is outside its valid range")
    void shouldRejectConfigurationWhenChanceIsOutsideValidRange() {
        assertThatThrownBy(
                        () ->
                                new VariableRewardConfiguration(
                                        new BigDecimal("-0.0001"),
                                        new BigDecimal("1"),
                                        new BigDecimal("100"),
                                        new BigDecimal("1000")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(
                        () ->
                                new VariableRewardConfiguration(
                                        new BigDecimal("1"),
                                        new BigDecimal("100.0001"),
                                        new BigDecimal("100"),
                                        new BigDecimal("1000")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should reject the configuration when the pool growth interval is not positive")
    void shouldRejectConfigurationWhenPoolGrowthIntervalIsNotPositive() {
        assertThatThrownBy(
                        () ->
                                new VariableRewardConfiguration(
                                        new BigDecimal("1"),
                                        new BigDecimal("1"),
                                        new BigDecimal("0.000000001"),
                                        new BigDecimal("1000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pool growth interval");
    }

    @Test
    @DisplayName("Should reject the configuration when the guaranteed pool is negative")
    void shouldRejectConfigurationWhenGuaranteedPoolIsNegative() {
        assertThatThrownBy(
                        () ->
                                new VariableRewardConfiguration(
                                        new BigDecimal("1"),
                                        new BigDecimal("1"),
                                        new BigDecimal("100"),
                                        new BigDecimal("-0.01")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Guaranteed win pool amount");
    }
}
