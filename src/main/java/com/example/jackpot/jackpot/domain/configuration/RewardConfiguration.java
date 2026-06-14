package com.example.jackpot.jackpot.domain.configuration;

import java.math.BigDecimal;
import java.util.Map;

public interface RewardConfiguration {

    String strategy();

    Map<String, BigDecimal> parameters();

    default void validateFor(BigDecimal initialPool) {}
}
