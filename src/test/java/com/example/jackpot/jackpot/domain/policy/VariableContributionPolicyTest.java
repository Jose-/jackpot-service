package com.example.jackpot.jackpot.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.jackpot.jackpot.domain.Jackpot;
import com.example.jackpot.jackpot.domain.configuration.FixedContributionConfiguration;
import com.example.jackpot.jackpot.domain.configuration.FixedRewardConfiguration;
import com.example.jackpot.jackpot.domain.configuration.VariableContributionConfiguration;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Variable contribution policy")
class VariableContributionPolicyTest {

    private static final UUID JACKPOT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final Instant NOW = Instant.parse("2026-06-12T10:16:00Z");
    private static final FixedRewardConfiguration REWARD_CONFIGURATION =
            new FixedRewardConfiguration(new BigDecimal("1"));

    private final VariableContributionPolicy policy = new VariableContributionPolicy();

    @Test
    @DisplayName("Should apply the initial rate when no pool growth interval is complete")
    void shouldApplyInitialRateWhenNoPoolGrowthIntervalIsComplete() {
        Jackpot jackpot = variableJackpot("1099.99");

        BigDecimal contribution = policy.calculate(new BigDecimal("100.00"), jackpot);

        assertThat(contribution).isEqualByComparingTo("5.00");
    }

    @Test
    @DisplayName("Should decrease the rate when a pool growth interval is completed")
    void shouldDecreaseRateWhenPoolGrowthIntervalIsCompleted() {
        Jackpot jackpot = variableJackpot("1100.00");

        BigDecimal contribution = policy.calculate(new BigDecimal("100.00"), jackpot);

        assertThat(contribution).isEqualByComparingTo("4.00");
    }

    @Test
    @DisplayName("Should decrease the rate after every completed pool growth interval")
    void shouldDecreaseRateAfterEveryCompletedPoolGrowthInterval() {
        Jackpot jackpot = variableJackpot("1200.00");

        BigDecimal contribution = policy.calculate(new BigDecimal("100.00"), jackpot);

        assertThat(contribution).isEqualByComparingTo("3.00");
    }

    @Test
    @DisplayName("Should apply the minimum rate when decreases reach the configured floor")
    void shouldApplyMinimumRateWhenDecreasesReachConfiguredFloor() {
        assertThat(policy.calculate(new BigDecimal("100.00"), variableJackpot("1300.00")))
                .isEqualByComparingTo("2.00");
        assertThat(policy.calculate(new BigDecimal("100.00"), variableJackpot("10000.00")))
                .isEqualByComparingTo("2.00");
    }

    @Test
    @DisplayName("Should preserve a fractional-cent contribution at ledger scale")
    void shouldPreserveFractionalCentContributionAtLedgerScale() {
        BigDecimal contribution =
                policy.calculate(new BigDecimal("10.10"), variableJackpot("1000.00"));

        assertThat(contribution).isEqualByComparingTo("0.50500000");
        assertThat(contribution.scale()).isEqualTo(8);
    }

    @Test
    @DisplayName("Should leave the jackpot unchanged when a contribution is calculated")
    void shouldLeaveJackpotUnchangedWhenContributionIsCalculated() {
        Jackpot jackpot = variableJackpot("1200.00");
        BigDecimal poolBeforeCalculation = jackpot.currentPool();

        policy.calculate(new BigDecimal("100.00"), jackpot);

        assertThat(jackpot.currentPool()).isEqualByComparingTo(poolBeforeCalculation);
    }

    @Test
    @DisplayName("Should reject creation when the current pool is below the initial pool")
    void shouldRejectCreationWhenCurrentPoolIsBelowInitialPool() {
        assertThatThrownBy(() -> variableJackpot("999.99"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Current pool");
    }

    @Test
    @DisplayName("Should reject the jackpot when it uses fixed contribution")
    void shouldRejectJackpotWhenItUsesFixedContribution() {
        Jackpot jackpot = fixedJackpot();

        assertThatThrownBy(() -> policy.calculate(new BigDecimal("10.00"), jackpot))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("variable contribution");
    }

    @Test
    @DisplayName("Should resolve the variable policy when it is registered")
    void shouldResolveVariablePolicyWhenItIsRegistered() {
        var fixedPolicy = new FixedContributionPolicy();
        var registry = new ContributionPolicyRegistry(List.of(fixedPolicy, policy));

        assertThat(registry.get(VariableContributionConfiguration.STRATEGY)).isSameAs(policy);
    }

    private static Jackpot variableJackpot(String currentPool) {
        return new Jackpot(
                JACKPOT_ID,
                new BigDecimal("1000"),
                new BigDecimal(currentPool),
                new VariableContributionConfiguration(
                        new BigDecimal("5"),
                        new BigDecimal("1"),
                        new BigDecimal("100"),
                        new BigDecimal("2")),
                REWARD_CONFIGURATION,
                NOW);
    }

    private static Jackpot fixedJackpot() {
        return new Jackpot(
                JACKPOT_ID,
                new BigDecimal("1000"),
                new BigDecimal("1000"),
                new FixedContributionConfiguration(new BigDecimal("5")),
                REWARD_CONFIGURATION,
                NOW);
    }
}
