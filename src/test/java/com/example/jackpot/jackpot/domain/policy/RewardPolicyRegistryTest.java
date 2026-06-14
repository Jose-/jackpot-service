package com.example.jackpot.jackpot.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.jackpot.jackpot.domain.RewardDecision;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Reward policy registry")
class RewardPolicyRegistryTest {

    private static final RewardPolicy FIXED_POLICY = new StubRewardPolicy("FIXED");
    private static final RewardPolicy VARIABLE_POLICY = new StubRewardPolicy("VARIABLE");

    @Test
    @DisplayName("Should return the matching policy when reward strategies are registered")
    void shouldReturnMatchingPolicyWhenRewardStrategiesAreRegistered() {
        var registry = new RewardPolicyRegistry(List.of(FIXED_POLICY, VARIABLE_POLICY));

        assertThat(registry.get("FIXED")).isSameAs(FIXED_POLICY);
        assertThat(registry.get("VARIABLE")).isSameAs(VARIABLE_POLICY);
    }

    @Test
    @DisplayName("Should reject policies when a reward strategy is duplicated")
    void shouldRejectPoliciesWhenRewardStrategyIsDuplicated() {
        assertThatThrownBy(
                        () ->
                                new RewardPolicyRegistry(
                                        List.of(FIXED_POLICY, FIXED_POLICY, VARIABLE_POLICY)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate")
                .hasMessageContaining("FIXED");
    }

    @Test
    @DisplayName("Should reject lookup when a reward strategy is not registered")
    void shouldRejectLookupWhenRewardStrategyIsNotRegistered() {
        var registry = new RewardPolicyRegistry(List.of(FIXED_POLICY));

        assertThatThrownBy(() -> registry.get("NEW_STRATEGY"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported")
                .hasMessageContaining("NEW_STRATEGY");
    }

    private record StubRewardPolicy(String strategy) implements RewardPolicy {
        @Override
        public RewardDecision evaluate(RewardEvaluationContext context) {
            return new RewardDecision(false, BigDecimal.ZERO, context.draw());
        }
    }
}
