package com.example.jackpot.jackpot.application;

import java.math.BigDecimal;
import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class SecureDrawGenerator implements DrawGenerator {
    private static final int DRAW_BOUND = 1_000_000;
    private final SecureRandom random = new SecureRandom();

    @Override
    public BigDecimal generate() {
        return BigDecimal.valueOf(random.nextInt(DRAW_BOUND), 4);
    }
}
