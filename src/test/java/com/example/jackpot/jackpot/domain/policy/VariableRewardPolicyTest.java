package com.example.jackpot.jackpot.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.jackpot.jackpot.domain.Jackpot;
import com.example.jackpot.jackpot.domain.configuration.FixedContributionConfiguration;
import com.example.jackpot.jackpot.domain.configuration.StoredRewardConfiguration;
import com.example.jackpot.jackpot.domain.configuration.VariableRewardConfiguration;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Variable reward policy")
class VariableRewardPolicyTest {

    private final VariableRewardPolicy policy = new VariableRewardPolicy();

    @Test
    @DisplayName("Should use initial win probability before a pool growth interval is completed")
    void shouldUseInitialWinProbabilityBeforePoolGrowthIntervalIsCompleted() {
        assertThat(
                        policy.evaluate(
                                        jackpot("1099.99"),
                                        new BigDecimal("10"),
                                        new BigDecimal("1.5"))
                                .calculatedChance())
                .isEqualByComparingTo("2.0000");
    }

    @Test
    @DisplayName("Should increase win probability after each completed pool growth interval")
    void shouldIncreaseWinProbabilityAfterEachCompletedPoolGrowthInterval() {
        assertThat(
                        policy.evaluate(
                                        jackpot("1200"),
                                        new BigDecimal("10"),
                                        new BigDecimal("3.5"))
                                .calculatedChance())
                .isEqualByComparingTo("4.0000");
    }

    @Test
    @DisplayName("Should cap win probability at one hundred")
    void shouldCapWinProbabilityAtOneHundred() {
        Jackpot jackpot = jackpot("1100", "90", "20", "10000");
        assertThat(
                        policy.evaluate(jackpot, new BigDecimal("10"), new BigDecimal("99.9999"))
                                .calculatedChance())
                .isEqualByComparingTo("100.0000");
    }

    @Test
    @DisplayName("Should use one hundred win probability when guaranteed win pool is reached")
    void shouldUseOneHundredWinProbabilityWhenGuaranteedWinPoolIsReached() {
        assertThat(
                        policy.evaluate(
                                        jackpot("10000"),
                                        new BigDecimal("10"),
                                        new BigDecimal("99.9999"))
                                .won())
                .isTrue();
    }

    @Test
    @DisplayName("Should own the win probability and draw comparison")
    void shouldOwnWinProbabilityAndDrawComparison() {
        assertThat(
                        policy.evaluate(
                                        jackpot("1200"),
                                        new BigDecimal("10"),
                                        new BigDecimal("3.9999"))
                                .won())
                .isTrue();
        assertThat(
                        policy.evaluate(jackpot("1200"), new BigDecimal("10"), new BigDecimal("4"))
                                .won())
                .isFalse();
    }

    @Test
    @DisplayName("Should reject evaluation when the guaranteed win pool is below the initial pool")
    void shouldRejectEvaluationWhenGuaranteedWinPoolIsBelowInitialPool() {
        Jackpot jackpot =
                new Jackpot(
                        UUID.fromString("11111111-1111-1111-1111-111111111111"),
                        new BigDecimal("1000"),
                        new BigDecimal("1000"),
                        new FixedContributionConfiguration(new BigDecimal("5")),
                        new StoredRewardConfiguration(
                                VariableRewardConfiguration.STRATEGY,
                                Map.of(
                                        "initialWinProbabilityPercentage", new BigDecimal("1"),
                                        "winProbabilityIncreasePerIntervalPercentagePoints",
                                                new BigDecimal("1"),
                                        "poolGrowthIntervalAmount", new BigDecimal("100"),
                                        "guaranteedWinPoolAmount", new BigDecimal("999.99"))),
                        Instant.parse("2026-06-12T10:16:00Z"));

        assertThatThrownBy(() -> policy.evaluate(jackpot, new BigDecimal("10"), BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Guaranteed win pool amount");
    }

    @Test
    @DisplayName("Should resolve the variable reward policy when it is registered")
    void shouldResolveVariableRewardPolicyWhenItIsRegistered() {
        assertThat(
                        new RewardPolicyRegistry(List.of(new FixedRewardPolicy(), policy))
                                .get(VariableRewardConfiguration.STRATEGY))
                .isSameAs(policy);
    }

    private static Jackpot jackpot(String currentPool) {
        return jackpot(currentPool, "2", "1", "10000");
    }

    private static Jackpot jackpot(
            String currentPool,
            String initialWinProbabilityPercentage,
            String winProbabilityIncreasePerIntervalPercentagePoints,
            String guaranteedWinPoolAmount) {
        return new Jackpot(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                new BigDecimal("1000"),
                new BigDecimal(currentPool),
                new FixedContributionConfiguration(new BigDecimal("5")),
                new VariableRewardConfiguration(
                        new BigDecimal(initialWinProbabilityPercentage),
                        new BigDecimal(winProbabilityIncreasePerIntervalPercentagePoints),
                        new BigDecimal("100"),
                        new BigDecimal(guaranteedWinPoolAmount)),
                Instant.parse("2026-06-12T10:16:00Z"));
    }
}
