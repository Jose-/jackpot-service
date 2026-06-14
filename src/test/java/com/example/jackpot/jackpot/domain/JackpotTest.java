package com.example.jackpot.jackpot.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.jackpot.jackpot.domain.configuration.FixedContributionConfiguration;
import com.example.jackpot.jackpot.domain.configuration.FixedRewardConfiguration;
import com.example.jackpot.jackpot.domain.configuration.VariableRewardConfiguration;
import com.example.jackpot.jackpot.domain.policy.ContributionPolicy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Jackpot aggregate")
class JackpotTest {

    private static final UUID JACKPOT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_JACKPOT_ID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID BET_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID USER_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final Instant NOW = Instant.parse("2026-06-12T10:16:00Z");
    private static final FixedContributionConfiguration CONTRIBUTION_CONFIGURATION =
            new FixedContributionConfiguration(new BigDecimal("5"));
    private static final FixedRewardConfiguration REWARD_CONFIGURATION =
            new FixedRewardConfiguration(new BigDecimal("1"));

    @Test
    @DisplayName("Should reject the jackpot when the initial pool is not positive")
    void shouldRejectJackpotWhenInitialPoolIsNotPositive() {
        assertThatThrownBy(() -> jackpot(BigDecimal.ZERO, BigDecimal.ZERO, REWARD_CONFIGURATION))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Initial pool");
    }

    @Test
    @DisplayName("Should reject the jackpot when the current pool is below the initial pool")
    void shouldRejectJackpotWhenCurrentPoolIsBelowInitialPool() {
        assertThatThrownBy(
                        () ->
                                jackpot(
                                        new BigDecimal("100"),
                                        new BigDecimal("99.99"),
                                        REWARD_CONFIGURATION))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Current pool");
    }

    @Test
    @DisplayName(
            "Should reject the jackpot when its reward configuration violates a strategy-specific invariant")
    void shouldRejectJackpotWhenRewardConfigurationViolatesStrategySpecificInvariant() {
        var variableReward =
                new VariableRewardConfiguration(
                        new BigDecimal("1"),
                        new BigDecimal("1"),
                        new BigDecimal("100"),
                        new BigDecimal("999.99"));

        assertThatThrownBy(
                        () ->
                                jackpot(
                                        new BigDecimal("1000"),
                                        new BigDecimal("1000"),
                                        variableReward))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Guaranteed win pool amount");
    }

    @Test
    @DisplayName("Should reject the bet when it targets another jackpot")
    void shouldRejectBetWhenItTargetsAnotherJackpot() {
        Jackpot jackpot =
                jackpot(new BigDecimal("1000"), new BigDecimal("1000"), REWARD_CONFIGURATION);
        Bet bet = bet(OTHER_JACKPOT_ID);

        assertThatThrownBy(() -> jackpot.contribute(bet, policyReturning("1.00"), NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("different jackpot");
    }

    @Test
    @DisplayName("Should increase the pool and return its snapshot when a bet contributes")
    void shouldIncreasePoolAndReturnItsSnapshotWhenBetContributes() {
        Jackpot jackpot =
                jackpot(new BigDecimal("1000"), new BigDecimal("1000"), REWARD_CONFIGURATION);
        Bet bet = bet(JACKPOT_ID);

        JackpotContribution contribution =
                jackpot.contribute(bet, policyReturning("0.00010000"), NOW);

        assertThat(jackpot.currentPool()).isEqualTo(new BigDecimal("1000.00010000"));
        assertThat(contribution.betId()).isEqualTo(BET_ID);
        assertThat(contribution.userId()).isEqualTo(USER_ID);
        assertThat(contribution.jackpotId()).isEqualTo(JACKPOT_ID);
        assertThat(contribution.stakeAmount()).isEqualByComparingTo("10.00");
        assertThat(contribution.contributionAmount()).isEqualTo(new BigDecimal("0.00010000"));
        assertThat(contribution.currentJackpotAmount()).isEqualTo(new BigDecimal("1000.00010000"));
        assertThat(contribution.createdAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("Should reject a zero contribution calculated by a policy")
    void shouldRejectZeroContributionCalculatedByPolicy() {
        Jackpot jackpot =
                jackpot(new BigDecimal("1000"), new BigDecimal("1000"), REWARD_CONFIGURATION);

        assertThatThrownBy(() -> jackpot.contribute(bet(JACKPOT_ID), policyReturning("0"), NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }

    @Test
    @DisplayName("Should reject a positive contribution that rounds to zero at ledger scale")
    void shouldRejectPositiveContributionThatRoundsToZeroAtLedgerScale() {
        Jackpot jackpot =
                jackpot(new BigDecimal("1000"), new BigDecimal("1000"), REWARD_CONFIGURATION);

        assertThatThrownBy(
                        () ->
                                jackpot.contribute(
                                        bet(JACKPOT_ID), policyReturning("0.000000001"), NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
        assertThat(jackpot.currentPool()).isEqualTo(new BigDecimal("1000.00000000"));
    }

    @Test
    @DisplayName("Should reject the contribution when the policy calculates a negative amount")
    void shouldRejectContributionWhenPolicyCalculatesNegativeAmount() {
        Jackpot jackpot =
                jackpot(new BigDecimal("1000"), new BigDecimal("1000"), REWARD_CONFIGURATION);

        assertThatThrownBy(() -> jackpot.contribute(bet(JACKPOT_ID), policyReturning("-0.01"), NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Contribution amount");
    }

    @Test
    @DisplayName("Should return the full pool and reset it when the jackpot is rewarded")
    void shouldReturnFullPoolAndResetItWhenJackpotIsRewarded() {
        Jackpot jackpot =
                jackpot(
                        new BigDecimal("1000"),
                        new BigDecimal("1250.00010000"),
                        REWARD_CONFIGURATION);

        BigDecimal reward = jackpot.rewardAndReset(NOW);

        assertThat(reward).isEqualTo(new BigDecimal("1250.00010000"));
        assertThat(jackpot.currentPool()).isEqualTo(new BigDecimal("1000.00000000"));
        assertThat(jackpot.updatedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("Should expose no public pool setter when the aggregate API is inspected")
    void shouldExposeNoPublicPoolSetterWhenAggregateApiIsInspected() {
        assertThat(
                        Arrays.stream(Jackpot.class.getMethods())
                                .map(method -> method.getName())
                                .filter(name -> name.startsWith("set"))
                                .toList())
                .isEmpty();
    }

    private static Jackpot jackpot(
            BigDecimal initialPool,
            BigDecimal currentPool,
            com.example.jackpot.jackpot.domain.configuration.RewardConfiguration
                    rewardConfiguration) {
        return new Jackpot(
                JACKPOT_ID,
                initialPool,
                currentPool,
                CONTRIBUTION_CONFIGURATION,
                rewardConfiguration,
                NOW);
    }

    private static Bet bet(UUID jackpotId) {
        return new Bet(BET_ID, USER_ID, jackpotId, new BigDecimal("10"), NOW);
    }

    private static ContributionPolicy policyReturning(String contribution) {
        return new ContributionPolicy() {
            @Override
            public String strategy() {
                return FixedContributionConfiguration.STRATEGY;
            }

            @Override
            public BigDecimal calculate(BigDecimal stake, Jackpot jackpot) {
                return new BigDecimal(contribution);
            }
        };
    }
}
