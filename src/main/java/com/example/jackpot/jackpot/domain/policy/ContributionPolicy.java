package com.example.jackpot.jackpot.domain.policy;

import com.example.jackpot.jackpot.domain.Jackpot;
import java.math.BigDecimal;

public interface ContributionPolicy {

    String strategy();

    BigDecimal calculate(BigDecimal stake, Jackpot jackpot);
}
