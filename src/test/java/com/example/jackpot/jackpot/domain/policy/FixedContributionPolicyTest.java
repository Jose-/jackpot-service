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

@DisplayName("Fixed contribution policy")
class FixedContributionPolicyTest {

    private static final UUID JACKPOT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final Instant NOW = Instant.parse("2026-06-12T10:16:00Z");
    private static final FixedRewardConfiguration REWARD_CONFIGURATION =
            new FixedRewardConfiguration(new BigDecimal("1"));

    private final FixedContributionPolicy policy = new FixedContributionPolicy();

    @Test
    @DisplayName(
            "Should calculate the configured percentage when the jackpot uses fixed contribution")
    void shouldCalculateConfiguredPercentageWhenJackpotUsesFixedContribution() {
        Jackpot jackpot = fixedJackpot("2.5000");

        BigDecimal contribution = policy.calculate(new BigDecimal("200.00"), jackpot);

        assertThat(contribution).isEqualByComparingTo("5.00");
    }

    @Test
    @DisplayName("Should preserve a fractional-cent contribution at ledger scale")
    void shouldPreserveFractionalCentContributionAtLedgerScale() {
        Jackpot jackpot = fixedJackpot("5.0000");

        BigDecimal contribution = policy.calculate(new BigDecimal("10.10"), jackpot);

        assertThat(contribution).isEqualByComparingTo("0.50500000");
        assertThat(contribution.scale()).isEqualTo(8);
    }

    @Test
    @DisplayName("Should produce a positive fractional contribution for a one-cent stake")
    void shouldProducePositiveFractionalContributionForOneCentStake() {
        Jackpot jackpot = fixedJackpot("1.0000");

        BigDecimal contribution = policy.calculate(new BigDecimal("0.01"), jackpot);

        assertThat(contribution).isEqualTo(new BigDecimal("0.00010000"));
    }

    @Test
    @DisplayName("Should leave the jackpot unchanged when a contribution is calculated")
    void shouldLeaveJackpotUnchangedWhenContributionIsCalculated() {
        Jackpot jackpot = fixedJackpot("5.0000");
        BigDecimal poolBeforeCalculation = jackpot.currentPool();

        policy.calculate(new BigDecimal("10.00"), jackpot);

        assertThat(jackpot.currentPool()).isEqualByComparingTo(poolBeforeCalculation);
    }

    @Test
    @DisplayName("Should reject the jackpot when it uses variable contribution")
    void shouldRejectJackpotWhenItUsesVariableContribution() {
        Jackpot jackpot = variableJackpot();

        assertThatThrownBy(() -> policy.calculate(new BigDecimal("10.00"), jackpot))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fixed contribution");
    }

    @Test
    @DisplayName("Should resolve the fixed policy when it is registered")
    void shouldResolveFixedPolicyWhenItIsRegistered() {
        ContributionPolicy variablePolicy =
                new ContributionPolicy() {
                    @Override
                    public String strategy() {
                        return VariableContributionConfiguration.STRATEGY;
                    }

                    @Override
                    public BigDecimal calculate(BigDecimal stake, Jackpot jackpot) {
                        return BigDecimal.ZERO;
                    }
                };
        var registry = new ContributionPolicyRegistry(List.of(policy, variablePolicy));

        assertThat(registry.get(FixedContributionConfiguration.STRATEGY)).isSameAs(policy);
    }

    private static Jackpot fixedJackpot(String percentage) {
        return new Jackpot(
                JACKPOT_ID,
                new BigDecimal("1000"),
                new BigDecimal("1000"),
                new FixedContributionConfiguration(new BigDecimal(percentage)),
                REWARD_CONFIGURATION,
                NOW);
    }

    private static Jackpot variableJackpot() {
        return new Jackpot(
                JACKPOT_ID,
                new BigDecimal("1000"),
                new BigDecimal("1000"),
                new VariableContributionConfiguration(
                        new BigDecimal("5"),
                        new BigDecimal("1"),
                        new BigDecimal("100"),
                        new BigDecimal("1")),
                REWARD_CONFIGURATION,
                NOW);
    }
}
