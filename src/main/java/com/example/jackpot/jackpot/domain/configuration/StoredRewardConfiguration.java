package com.example.jackpot.jackpot.domain.configuration;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public record StoredRewardConfiguration(String strategy, Map<String, BigDecimal> parameters)
        implements RewardConfiguration {

    public StoredRewardConfiguration {
        strategy = Objects.requireNonNull(strategy, "strategy must not be null");
        parameters = Map.copyOf(Objects.requireNonNull(parameters, "parameters must not be null"));
    }
}
