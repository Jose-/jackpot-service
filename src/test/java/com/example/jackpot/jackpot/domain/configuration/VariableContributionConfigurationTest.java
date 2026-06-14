package com.example.jackpot.jackpot.domain.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Variable contribution configuration")
class VariableContributionConfigurationTest {

    @Test
    @DisplayName("Should normalize every scalar when the configuration is valid")
    void shouldNormalizeEveryScalarWhenConfigurationIsValid() {
        var configuration =
                new VariableContributionConfiguration(
                        new BigDecimal("5.12345"),
                        new BigDecimal("0.50005"),
                        new BigDecimal("100.005"),
                        new BigDecimal("1.23454"));

        assertThat(configuration.initialRatePercentage()).isEqualByComparingTo("5.1235");
        assertThat(configuration.rateDecreasePerIntervalPercentagePoints())
                .isEqualByComparingTo("0.5001");
        assertThat(configuration.poolGrowthIntervalAmount())
                .isEqualTo(new BigDecimal("100.00500000"));
        assertThat(configuration.minimumRatePercentage()).isEqualByComparingTo("1.2345");
    }

    @Test
    @DisplayName("Should reject the configuration when a required scalar is null")
    void shouldRejectConfigurationWhenRequiredScalarIsNull() {
        assertThatNullPointerException()
                .isThrownBy(
                        () ->
                                new VariableContributionConfiguration(
                                        null,
                                        new BigDecimal("1"),
                                        new BigDecimal("100"),
                                        new BigDecimal("1")))
                .withMessageContaining("initial rate percentage");
        assertThatNullPointerException()
                .isThrownBy(
                        () ->
                                new VariableContributionConfiguration(
                                        new BigDecimal("5"),
                                        null,
                                        new BigDecimal("100"),
                                        new BigDecimal("1")))
                .withMessageContaining("rate decrease per interval percentage points");
        assertThatNullPointerException()
                .isThrownBy(
                        () ->
                                new VariableContributionConfiguration(
                                        new BigDecimal("5"),
                                        new BigDecimal("1"),
                                        null,
                                        new BigDecimal("1")))
                .withMessageContaining("pool growth interval amount");
        assertThatNullPointerException()
                .isThrownBy(
                        () ->
                                new VariableContributionConfiguration(
                                        new BigDecimal("5"),
                                        new BigDecimal("1"),
                                        new BigDecimal("100"),
                                        null))
                .withMessageContaining("minimum rate percentage");
    }

    @Test
    @DisplayName("Should reject the configuration when a percentage is outside its valid range")
    void shouldRejectConfigurationWhenPercentageIsOutsideValidRange() {
        assertThatThrownBy(
                        () ->
                                new VariableContributionConfiguration(
                                        new BigDecimal("0"),
                                        new BigDecimal("1"),
                                        new BigDecimal("100"),
                                        new BigDecimal("1")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(
                        () ->
                                new VariableContributionConfiguration(
                                        new BigDecimal("5"),
                                        new BigDecimal("100.0001"),
                                        new BigDecimal("100"),
                                        new BigDecimal("1")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(
                        () ->
                                new VariableContributionConfiguration(
                                        new BigDecimal("5"),
                                        new BigDecimal("1"),
                                        new BigDecimal("100"),
                                        new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName(
            "Should reject the configuration when the minimum percentage exceeds the initial percentage")
    void shouldRejectConfigurationWhenMinimumPercentageExceedsInitialPercentage() {
        assertThatThrownBy(
                        () ->
                                new VariableContributionConfiguration(
                                        new BigDecimal("5"),
                                        new BigDecimal("1"),
                                        new BigDecimal("100"),
                                        new BigDecimal("6")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Minimum rate percentage");
    }

    @Test
    @DisplayName("Should reject the configuration when the pool growth interval is not positive")
    void shouldRejectConfigurationWhenPoolGrowthIntervalIsNotPositive() {
        assertThatThrownBy(
                        () ->
                                new VariableContributionConfiguration(
                                        new BigDecimal("5"),
                                        new BigDecimal("1"),
                                        new BigDecimal("0.000000001"),
                                        new BigDecimal("1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pool growth interval");
    }
}
