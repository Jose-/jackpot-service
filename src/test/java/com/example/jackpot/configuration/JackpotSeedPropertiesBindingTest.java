package com.example.jackpot.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

@DisplayName("Jackpot seed properties binding")
class JackpotSeedPropertiesBindingTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(PropertiesConfiguration.class);

    @Test
    @DisplayName("Should bind fixed and variable jackpots from the nested structure")
    void shouldBindFixedAndVariableJackpotsFromNestedStructure() {
        contextRunner
                .withPropertyValues(validFixedJackpot())
                .withPropertyValues(validVariableJackpot())
                .run(
                        context -> {
                            assertThat(context).hasNotFailed();
                            var properties = context.getBean(JackpotSeedProperties.class);

                            var fixed = properties.jackpots().get(0);
                            assertThat(fixed.contribution().strategy())
                                    .isEqualTo(JackpotSeedProperties.Strategy.FIXED);
                            assertThat(fixed.contribution().fixed().ratePercentage())
                                    .isEqualByComparingTo("5.0000");
                            assertThat(fixed.reward().fixed().winProbabilityPercentage())
                                    .isEqualByComparingTo("1.0000");

                            var variable = properties.jackpots().get(1);
                            assertThat(variable.contribution().strategy())
                                    .isEqualTo(JackpotSeedProperties.Strategy.VARIABLE);
                            assertThat(variable.contribution().variable().initialRatePercentage())
                                    .isEqualByComparingTo("10.0000");
                            assertThat(
                                            variable.contribution()
                                                    .variable()
                                                    .rateDecreasePerIntervalPercentagePoints())
                                    .isEqualByComparingTo("1.0000");
                            assertThat(
                                            variable.contribution()
                                                    .variable()
                                                    .poolGrowthIntervalAmount())
                                    .isEqualByComparingTo("1000.00");
                            assertThat(variable.contribution().variable().minimumRatePercentage())
                                    .isEqualByComparingTo("2.0000");
                            assertThat(
                                            variable.reward()
                                                    .variable()
                                                    .initialWinProbabilityPercentage())
                                    .isEqualByComparingTo("0.5000");
                            assertThat(
                                            variable.reward()
                                                    .variable()
                                                    .winProbabilityIncreasePerIntervalPercentagePoints())
                                    .isEqualByComparingTo("0.5000");
                            assertThat(variable.reward().variable().poolGrowthIntervalAmount())
                                    .isEqualByComparingTo("500.00");
                            assertThat(variable.reward().variable().guaranteedWinPoolAmount())
                                    .isEqualByComparingTo("5000.00");
                        });
    }

    @Test
    @DisplayName("Should fail clearly when contribution strategy is absent")
    void shouldFailClearlyWhenContributionStrategyIsAbsent() {
        contextRunner
                .withPropertyValues(validFixedJackpot())
                .withPropertyValues("jackpot.seed.jackpots[0].contribution.strategy=")
                .run(
                        context -> {
                            assertThat(context).hasFailed();
                            assertThat(context.getStartupFailure())
                                    .hasRootCauseMessage("contribution strategy must not be null");
                        });
    }

    @Test
    @DisplayName("Should fail clearly when selected fixed contribution is absent")
    void shouldFailClearlyWhenSelectedFixedContributionIsAbsent() {
        contextRunner
                .withPropertyValues(validFixedJackpot())
                .withPropertyValues("jackpot.seed.jackpots[0].contribution.fixed.rate-percentage=")
                .run(
                        context -> {
                            assertThat(context).hasFailed();
                            assertThat(context.getStartupFailure())
                                    .hasRootCauseMessage(
                                            "contribution.fixed is required when contribution strategy is FIXED");
                        });
    }

    @Test
    @DisplayName("Should fail clearly when variable contribution interval is absent")
    void shouldFailClearlyWhenVariableContributionIntervalIsAbsent() {
        contextRunner
                .withPropertyValues(validFixedJackpot())
                .withPropertyValues(validVariableJackpot())
                .withPropertyValues(
                        "jackpot.seed.jackpots[1].contribution.variable.pool-growth-interval-amount=")
                .run(
                        context -> {
                            assertThat(context).hasFailed();
                            assertThat(context.getStartupFailure())
                                    .hasRootCauseMessage(
                                            "contribution variable pool growth interval amount must not be null");
                        });
    }

    @Test
    @DisplayName("Should fail clearly when variable reward guaranteed win pool is absent")
    void shouldFailClearlyWhenVariableRewardGuaranteedWinPoolIsAbsent() {
        contextRunner
                .withPropertyValues(validFixedJackpot())
                .withPropertyValues(validVariableJackpot())
                .withPropertyValues(
                        "jackpot.seed.jackpots[1].reward.variable.guaranteed-win-pool-amount=")
                .run(
                        context -> {
                            assertThat(context).hasFailed();
                            assertThat(context.getStartupFailure())
                                    .hasRootCauseMessage(
                                            "reward variable guaranteed win pool amount must not be null");
                        });
    }

    private static String[] validFixedJackpot() {
        return new String[] {
            "jackpot.seed.jackpots[0].id=11111111-1111-1111-1111-111111111111",
            "jackpot.seed.jackpots[0].name=Fixed jackpot",
            "jackpot.seed.jackpots[0].initial-pool-amount=1000",
            "jackpot.seed.jackpots[0].contribution.strategy=FIXED",
            "jackpot.seed.jackpots[0].contribution.fixed.rate-percentage=5.0000",
            "jackpot.seed.jackpots[0].reward.strategy=FIXED",
            "jackpot.seed.jackpots[0].reward.fixed.win-probability-percentage=1.0000"
        };
    }

    private static String[] validVariableJackpot() {
        return new String[] {
            "jackpot.seed.jackpots[1].id=22222222-2222-2222-2222-222222222222",
            "jackpot.seed.jackpots[1].name=Variable jackpot",
            "jackpot.seed.jackpots[1].initial-pool-amount=500",
            "jackpot.seed.jackpots[1].contribution.strategy=VARIABLE",
            "jackpot.seed.jackpots[1].contribution.variable.initial-rate-percentage=10.0000",
            "jackpot.seed.jackpots[1].contribution.variable.rate-decrease-per-interval-percentage-points=1.0000",
            "jackpot.seed.jackpots[1].contribution.variable.pool-growth-interval-amount=1000.00",
            "jackpot.seed.jackpots[1].contribution.variable.minimum-rate-percentage=2.0000",
            "jackpot.seed.jackpots[1].reward.strategy=VARIABLE",
            "jackpot.seed.jackpots[1].reward.variable.initial-win-probability-percentage=0.5000",
            "jackpot.seed.jackpots[1].reward.variable.win-probability-increase-per-interval-percentage-points=0.5000",
            "jackpot.seed.jackpots[1].reward.variable.pool-growth-interval-amount=500.00",
            "jackpot.seed.jackpots[1].reward.variable.guaranteed-win-pool-amount=5000.00"
        };
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(JackpotSeedProperties.class)
    static class PropertiesConfiguration {}
}
