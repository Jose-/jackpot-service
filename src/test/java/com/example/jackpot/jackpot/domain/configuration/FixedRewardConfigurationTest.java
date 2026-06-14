package com.example.jackpot.jackpot.domain.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Fixed reward configuration")
class FixedRewardConfigurationTest {

    @Test
    @DisplayName("Should allow zero win probability when the configuration is valid")
    void shouldAllowZeroWinProbabilityWhenConfigurationIsValid() {
        var configuration = new FixedRewardConfiguration(BigDecimal.ZERO);

        assertThat(configuration.winProbabilityPercentage()).isEqualByComparingTo("0.0000");
    }

    @Test
    @DisplayName("Should normalize the win probability when the configuration is valid")
    void shouldNormalizeWinProbabilityWhenConfigurationIsValid() {
        var configuration = new FixedRewardConfiguration(new BigDecimal("2.34567"));

        assertThat(configuration.winProbabilityPercentage()).isEqualByComparingTo("2.3457");
    }

    @Test
    @DisplayName("Should reject the win probability when it is outside its valid range")
    void shouldRejectWinProbabilityWhenItIsOutsideValidRange() {
        assertThatThrownBy(() -> new FixedRewardConfiguration(new BigDecimal("-0.0001")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FixedRewardConfiguration(new BigDecimal("100.0001")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should reject the win probability when it is null")
    void shouldRejectWinProbabilityWhenItIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new FixedRewardConfiguration(null))
                .withMessageContaining("win probability");
    }
}
