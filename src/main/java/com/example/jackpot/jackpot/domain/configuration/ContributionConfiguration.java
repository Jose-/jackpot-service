package com.example.jackpot.jackpot.domain.configuration;

import java.math.BigDecimal;
import java.util.Map;

public interface ContributionConfiguration {
    String strategy();

    Map<String, BigDecimal> parameters();
}
