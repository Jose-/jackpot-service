package com.example.jackpot.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jackpot.jackpot.persistence.JackpotRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "jackpot.messaging.mode=log")
@DisplayName("Jackpot seeder")
class JackpotSeederTest {
    @Autowired JackpotSeeder seeder;
    @Autowired JackpotRepository repository;
    @Autowired JackpotSeedProperties properties;

    @Test
    @DisplayName("Should seed two stable jackpots without duplicating them when rerun")
    void shouldSeedTwoStableJackpotsWithoutDuplicatingThemWhenRerun() {
        assertThat(repository.findAll()).hasSize(2);
        assertThat(repository.existsById(properties.fixedId())).isTrue();
        assertThat(repository.existsById(properties.variableId())).isTrue();
        seeder.run(null);
        assertThat(repository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("Should seed every jackpot value from nested configuration")
    void shouldSeedEveryJackpotValueFromNestedConfiguration() {
        var fixedProperties = definition(JackpotSeedProperties.Strategy.FIXED);
        var fixedEntity = repository.findById(fixedProperties.id()).orElseThrow();
        var fixed = fixedEntity.toDomain();
        var fixedContribution = fixed.contributionConfiguration().parameters();

        assertThat(fixedEntity.name()).isEqualTo(fixedProperties.name());
        assertThat(fixed.initialPool()).isEqualByComparingTo(fixedProperties.initialPoolAmount());
        assertThat(fixed.currentPool()).isEqualByComparingTo(fixedProperties.initialPoolAmount());
        assertThat(fixedContribution.get("percentage"))
                .isEqualByComparingTo(fixedProperties.contribution().fixed().ratePercentage());
        assertThat(fixed.rewardConfiguration().parameters().get("chance"))
                .isEqualByComparingTo(fixedProperties.reward().fixed().winProbabilityPercentage());

        var variableProperties = definition(JackpotSeedProperties.Strategy.VARIABLE);
        var variableEntity = repository.findById(variableProperties.id()).orElseThrow();
        var variable = variableEntity.toDomain();
        var variableContribution = variable.contributionConfiguration().parameters();
        var variableReward = variable.rewardConfiguration().parameters();

        assertThat(variableEntity.name()).isEqualTo(variableProperties.name());
        assertThat(variable.initialPool())
                .isEqualByComparingTo(variableProperties.initialPoolAmount());
        assertThat(variable.currentPool())
                .isEqualByComparingTo(variableProperties.initialPoolAmount());
        assertThat(variableContribution.get("initialRatePercentage"))
                .isEqualByComparingTo(
                        variableProperties.contribution().variable().initialRatePercentage());
        assertThat(variableContribution.get("rateDecreasePerIntervalPercentagePoints"))
                .isEqualByComparingTo(
                        variableProperties
                                .contribution()
                                .variable()
                                .rateDecreasePerIntervalPercentagePoints());
        assertThat(variableContribution.get("poolGrowthIntervalAmount"))
                .isEqualByComparingTo(
                        variableProperties.contribution().variable().poolGrowthIntervalAmount());
        assertThat(variableContribution.get("minimumRatePercentage"))
                .isEqualByComparingTo(
                        variableProperties.contribution().variable().minimumRatePercentage());
        assertThat(variableReward.get("initialWinProbabilityPercentage"))
                .isEqualByComparingTo(
                        variableProperties.reward().variable().initialWinProbabilityPercentage());
        assertThat(variableReward.get("winProbabilityIncreasePerIntervalPercentagePoints"))
                .isEqualByComparingTo(
                        variableProperties
                                .reward()
                                .variable()
                                .winProbabilityIncreasePerIntervalPercentagePoints());
        assertThat(variableReward.get("poolGrowthIntervalAmount"))
                .isEqualByComparingTo(
                        variableProperties.reward().variable().poolGrowthIntervalAmount());
        assertThat(variableReward.get("guaranteedWinPoolAmount"))
                .isEqualByComparingTo(
                        variableProperties.reward().variable().guaranteedWinPoolAmount());
    }

    private JackpotSeedProperties.Definition definition(JackpotSeedProperties.Strategy strategy) {
        return properties.jackpots().stream()
                .filter(definition -> definition.contribution().strategy() == strategy)
                .findFirst()
                .orElseThrow();
    }
}
