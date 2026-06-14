package com.example.jackpot.jackpot.domain.configuration;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public record StoredContributionConfiguration(String strategy, Map<String, BigDecimal> parameters)
        implements ContributionConfiguration {
    public StoredContributionConfiguration {
        strategy = Objects.requireNonNull(strategy, "strategy must not be null");
        parameters = Map.copyOf(Objects.requireNonNull(parameters, "parameters must not be null"));
    }
}
