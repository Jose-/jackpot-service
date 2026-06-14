package com.example.jackpot.jackpot.domain.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Fixed contribution configuration")
class FixedContributionConfigurationTest {

    @Test
    @DisplayName("Should normalize the rate percentage when the configuration is valid")
    void shouldNormalizeRatePercentageWhenConfigurationIsValid() {
        var configuration = new FixedContributionConfiguration(new BigDecimal("2.34567"));

        assertThat(configuration.ratePercentage()).isEqualByComparingTo("2.3457");
    }

    @Test
    @DisplayName("Should reject the rate percentage when it rounds to zero")
    void shouldRejectRatePercentageWhenItRoundsToZero() {
        assertThatThrownBy(() -> new FixedContributionConfiguration(new BigDecimal("0.00004")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("percentage");
    }

    @Test
    @DisplayName("Should reject the rate percentage when it exceeds one hundred")
    void shouldRejectRatePercentageWhenItExceedsOneHundred() {
        assertThatThrownBy(() -> new FixedContributionConfiguration(new BigDecimal("100.0001")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("percentage");
    }

    @Test
    @DisplayName("Should reject the rate percentage when it is null")
    void shouldRejectRatePercentageWhenItIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new FixedContributionConfiguration(null))
                .withMessageContaining("percentage");
    }
}
