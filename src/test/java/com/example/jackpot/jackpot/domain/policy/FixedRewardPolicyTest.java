package com.example.jackpot.jackpot.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jackpot.jackpot.domain.Jackpot;
import com.example.jackpot.jackpot.domain.configuration.FixedContributionConfiguration;
import com.example.jackpot.jackpot.domain.configuration.FixedRewardConfiguration;
import com.example.jackpot.jackpot.domain.configuration.VariableRewardConfiguration;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Fixed reward policy")
class FixedRewardPolicyTest {

    private final FixedRewardPolicy policy = new FixedRewardPolicy();

    @Test
    @DisplayName("Should win when the draw is lower than the fixed win probability")
    void shouldWinWhenDrawIsLowerThanFixedWinProbability() {
        var decision =
                policy.evaluate(jackpot("2.5"), new BigDecimal("10"), new BigDecimal("2.4999"));
        assertThat(decision.won()).isTrue();
        assertThat(decision.calculatedChance()).isEqualByComparingTo("2.5000");
    }

    @Test
    @DisplayName("Should lose when the draw equals the fixed win probability")
    void shouldLoseWhenDrawEqualsFixedWinProbability() {
        assertThat(
                        policy.evaluate(jackpot("2.5"), new BigDecimal("10"), new BigDecimal("2.5"))
                                .won())
                .isFalse();
    }

    @Test
    @DisplayName("Should always lose when fixed win probability is zero")
    void shouldAlwaysLoseWhenFixedWinProbabilityIsZero() {
        assertThat(policy.evaluate(jackpot("0"), new BigDecimal("10"), BigDecimal.ZERO).won())
                .isFalse();
    }

    @Test
    @DisplayName("Should always win for a valid draw when fixed win probability is one hundred")
    void shouldAlwaysWinForValidDrawWhenFixedWinProbabilityIsOneHundred() {
        assertThat(
                        policy.evaluate(
                                        jackpot("100"),
                                        new BigDecimal("10"),
                                        new BigDecimal("99.9999"))
                                .won())
                .isTrue();
    }

    @Test
    @DisplayName("Should resolve the fixed reward policy when it is registered")
    void shouldResolveFixedRewardPolicyWhenItIsRegistered() {
        RewardPolicy variable =
                new RewardPolicy() {
                    public String strategy() {
                        return VariableRewardConfiguration.STRATEGY;
                    }

                    public com.example.jackpot.jackpot.domain.RewardDecision evaluate(
                            RewardEvaluationContext context) {
                        return new com.example.jackpot.jackpot.domain.RewardDecision(
                                false, BigDecimal.ZERO, context.draw());
                    }
                };
        assertThat(
                        new RewardPolicyRegistry(List.of(policy, variable))
                                .get(FixedRewardConfiguration.STRATEGY))
                .isSameAs(policy);
    }

    private static Jackpot jackpot(String chance) {
        return new Jackpot(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                new BigDecimal("1000"),
                new BigDecimal("1000"),
                new FixedContributionConfiguration(new BigDecimal("5")),
                new FixedRewardConfiguration(new BigDecimal(chance)),
                Instant.parse("2026-06-12T10:16:00Z"));
    }
}
