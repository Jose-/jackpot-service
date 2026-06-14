package com.example.jackpot.jackpot.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Reward decision")
class RewardDecisionTest {

    @Test
    @DisplayName("Should preserve normalized chance and draw when outcome is consistent")
    void shouldPreserveNormalizedChanceAndDrawWhenOutcomeIsConsistent() {
        var decision =
                new RewardDecision(true, new BigDecimal("2.50004"), new BigDecimal("2.49999"));
        assertThat(decision.calculatedChance()).isEqualByComparingTo("2.5000");
        assertThat(decision.generatedDraw()).isEqualByComparingTo("2.4999");
    }

    @Test
    @DisplayName("Should reject the decision when outcome contradicts chance and draw")
    void shouldRejectDecisionWhenOutcomeContradictsChanceAndDraw() {
        assertThatThrownBy(() -> new RewardDecision(true, new BigDecimal("2"), new BigDecimal("2")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
