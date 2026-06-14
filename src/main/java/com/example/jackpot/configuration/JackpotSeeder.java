package com.example.jackpot.configuration;

import com.example.jackpot.jackpot.domain.Jackpot;
import com.example.jackpot.jackpot.domain.configuration.ContributionConfiguration;
import com.example.jackpot.jackpot.domain.configuration.FixedContributionConfiguration;
import com.example.jackpot.jackpot.domain.configuration.FixedRewardConfiguration;
import com.example.jackpot.jackpot.domain.configuration.RewardConfiguration;
import com.example.jackpot.jackpot.domain.configuration.VariableContributionConfiguration;
import com.example.jackpot.jackpot.domain.configuration.VariableRewardConfiguration;
import com.example.jackpot.jackpot.persistence.JackpotEntity;
import com.example.jackpot.jackpot.persistence.JackpotRepository;
import java.time.Clock;
import java.time.Instant;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JackpotSeeder implements ApplicationRunner {
    private final JackpotRepository repository;
    private final JackpotSeedProperties properties;
    private final Clock clock;

    public JackpotSeeder(
            JackpotRepository repository, JackpotSeedProperties properties, Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Instant now = clock.instant();
        properties.jackpots().stream()
                .filter(definition -> !repository.existsById(definition.id()))
                .map(definition -> entity(definition, now))
                .forEach(repository::save);
    }

    private JackpotEntity entity(JackpotSeedProperties.Definition definition, Instant now) {
        return new JackpotEntity(
                definition.name(),
                new Jackpot(
                        definition.id(),
                        definition.initialPoolAmount(),
                        definition.initialPoolAmount(),
                        contributionConfiguration(definition.contribution()),
                        rewardConfiguration(definition.reward()),
                        now));
    }

    private ContributionConfiguration contributionConfiguration(
            JackpotSeedProperties.Contribution definition) {
        return switch (definition.strategy()) {
            case FIXED -> new FixedContributionConfiguration(definition.fixed().ratePercentage());
            case VARIABLE ->
                    new VariableContributionConfiguration(
                            definition.variable().initialRatePercentage(),
                            definition.variable().rateDecreasePerIntervalPercentagePoints(),
                            definition.variable().poolGrowthIntervalAmount(),
                            definition.variable().minimumRatePercentage());
        };
    }

    private RewardConfiguration rewardConfiguration(JackpotSeedProperties.Reward definition) {
        return switch (definition.strategy()) {
            case FIXED ->
                    new FixedRewardConfiguration(definition.fixed().winProbabilityPercentage());
            case VARIABLE ->
                    new VariableRewardConfiguration(
                            definition.variable().initialWinProbabilityPercentage(),
                            definition
                                    .variable()
                                    .winProbabilityIncreasePerIntervalPercentagePoints(),
                            definition.variable().poolGrowthIntervalAmount(),
                            definition.variable().guaranteedWinPoolAmount());
        };
    }
}
