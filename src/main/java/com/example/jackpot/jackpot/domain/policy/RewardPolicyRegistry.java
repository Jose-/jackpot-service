package com.example.jackpot.jackpot.domain.policy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class RewardPolicyRegistry {

    private final Map<String, RewardPolicy> policies;

    public RewardPolicyRegistry(Collection<? extends RewardPolicy> policies) {
        Objects.requireNonNull(policies, "policies must not be null");

        this.policies = new HashMap<>();
        for (RewardPolicy policy : policies) {
            Objects.requireNonNull(policy, "policy must not be null");
            String strategy =
                    Objects.requireNonNull(policy.strategy(), "strategy must not be null");
            if (this.policies.putIfAbsent(strategy, policy) != null) {
                throw new IllegalStateException("Duplicate reward policy for " + strategy);
            }
        }
    }

    public RewardPolicy get(String strategy) {
        strategy = Objects.requireNonNull(strategy, "strategy must not be null");
        RewardPolicy policy = policies.get(strategy);
        if (policy == null) {
            throw new IllegalArgumentException("Unsupported reward strategy: " + strategy);
        }
        return policy;
    }
}
