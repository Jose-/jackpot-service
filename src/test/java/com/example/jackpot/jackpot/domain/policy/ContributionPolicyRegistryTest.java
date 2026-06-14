package com.example.jackpot.jackpot.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.jackpot.jackpot.domain.Jackpot;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Contribution policy registry")
class ContributionPolicyRegistryTest {

    private static final ContributionPolicy FIXED_POLICY = new StubContributionPolicy("FIXED");
    private static final ContributionPolicy VARIABLE_POLICY =
            new StubContributionPolicy("VARIABLE");

    @Test
    @DisplayName("Should return the matching policy when every contribution type is registered")
    void shouldReturnMatchingPolicyWhenEveryContributionTypeIsRegistered() {
        var registry = new ContributionPolicyRegistry(List.of(FIXED_POLICY, VARIABLE_POLICY));

        assertThat(registry.get("FIXED")).isSameAs(FIXED_POLICY);
        assertThat(registry.get("VARIABLE")).isSameAs(VARIABLE_POLICY);
    }

    @Test
    @DisplayName("Should reject policies when a contribution type is duplicated")
    void shouldRejectPoliciesWhenContributionTypeIsDuplicated() {
        assertThatThrownBy(
                        () ->
                                new ContributionPolicyRegistry(
                                        List.of(FIXED_POLICY, FIXED_POLICY, VARIABLE_POLICY)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate")
                .hasMessageContaining("FIXED");
    }

    @Test
    @DisplayName("Should reject an unknown contribution strategy")
    void shouldRejectUnknownContributionStrategy() {
        assertThatThrownBy(() -> new ContributionPolicyRegistry(List.of(FIXED_POLICY)).get("THIRD"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown")
                .hasMessageContaining("THIRD");
    }

    private record StubContributionPolicy(String strategy) implements ContributionPolicy {

        @Override
        public BigDecimal calculate(BigDecimal stake, Jackpot jackpot) {
            return BigDecimal.ZERO;
        }
    }
}
