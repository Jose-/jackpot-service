package com.example.jackpot.configuration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("jackpot.seed")
public record JackpotSeedProperties(List<Definition> jackpots) {

    public JackpotSeedProperties {
        jackpots = List.copyOf(Objects.requireNonNull(jackpots, "jackpots must not be null"));
        if (jackpots.isEmpty()) {
            throw new IllegalArgumentException("At least one jackpot seed definition is required");
        }
    }

    public UUID fixedId() {
        return definitionFor(Strategy.FIXED).id();
    }

    public UUID variableId() {
        return definitionFor(Strategy.VARIABLE).id();
    }

    private Definition definitionFor(Strategy strategy) {
        return jackpots.stream()
                .filter(
                        definition ->
                                definition.contribution().strategy() == strategy
                                        && definition.reward().strategy() == strategy)
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "No seed jackpot uses " + strategy + " strategies"));
    }

    public enum Strategy {
        FIXED,
        VARIABLE
    }

    public record Definition(
            UUID id,
            String name,
            BigDecimal initialPoolAmount,
            Contribution contribution,
            Reward reward) {
        public Definition {
            Objects.requireNonNull(id, "jackpot id must not be null");
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("jackpot name must not be blank");
            }
            Objects.requireNonNull(initialPoolAmount, "initial pool amount must not be null");
            Objects.requireNonNull(contribution, "contribution configuration must not be null");
            Objects.requireNonNull(reward, "reward configuration must not be null");
        }
    }

    public record Contribution(
            Strategy strategy, FixedContribution fixed, VariableContribution variable) {
        public Contribution {
            Objects.requireNonNull(strategy, "contribution strategy must not be null");
            switch (strategy) {
                case FIXED ->
                        Objects.requireNonNull(
                                fixed,
                                "contribution.fixed is required when contribution strategy is FIXED");
                case VARIABLE ->
                        Objects.requireNonNull(
                                variable,
                                "contribution.variable is required when contribution strategy is VARIABLE");
            }
        }
    }

    public record FixedContribution(BigDecimal ratePercentage) {
        public FixedContribution {
            Objects.requireNonNull(
                    ratePercentage, "contribution fixed rate percentage must not be null");
        }
    }

    public record VariableContribution(
            BigDecimal initialRatePercentage,
            BigDecimal rateDecreasePerIntervalPercentagePoints,
            BigDecimal poolGrowthIntervalAmount,
            BigDecimal minimumRatePercentage) {
        public VariableContribution {
            Objects.requireNonNull(
                    initialRatePercentage,
                    "contribution variable initial rate percentage must not be null");
            Objects.requireNonNull(
                    rateDecreasePerIntervalPercentagePoints,
                    "contribution variable rate decrease per interval percentage points must not be null");
            Objects.requireNonNull(
                    poolGrowthIntervalAmount,
                    "contribution variable pool growth interval amount must not be null");
            Objects.requireNonNull(
                    minimumRatePercentage,
                    "contribution variable minimum rate percentage must not be null");
        }
    }

    public record Reward(Strategy strategy, FixedReward fixed, VariableReward variable) {
        public Reward {
            Objects.requireNonNull(strategy, "reward strategy must not be null");
            switch (strategy) {
                case FIXED ->
                        Objects.requireNonNull(
                                fixed, "reward.fixed is required when reward strategy is FIXED");
                case VARIABLE ->
                        Objects.requireNonNull(
                                variable,
                                "reward.variable is required when reward strategy is VARIABLE");
            }
        }
    }

    public record FixedReward(BigDecimal winProbabilityPercentage) {
        public FixedReward {
            Objects.requireNonNull(
                    winProbabilityPercentage,
                    "reward fixed win probability percentage must not be null");
        }
    }

    public record VariableReward(
            BigDecimal initialWinProbabilityPercentage,
            BigDecimal winProbabilityIncreasePerIntervalPercentagePoints,
            BigDecimal poolGrowthIntervalAmount,
            BigDecimal guaranteedWinPoolAmount) {
        public VariableReward {
            Objects.requireNonNull(
                    initialWinProbabilityPercentage,
                    "reward variable initial win probability percentage must not be null");
            Objects.requireNonNull(
                    winProbabilityIncreasePerIntervalPercentagePoints,
                    "reward variable win probability increase per interval percentage points must not be null");
            Objects.requireNonNull(
                    poolGrowthIntervalAmount,
                    "reward variable pool growth interval amount must not be null");
            Objects.requireNonNull(
                    guaranteedWinPoolAmount,
                    "reward variable guaranteed win pool amount must not be null");
        }
    }
}
