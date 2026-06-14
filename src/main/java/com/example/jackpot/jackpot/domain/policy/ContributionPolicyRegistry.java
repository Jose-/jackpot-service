package com.example.jackpot.jackpot.domain.policy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ContributionPolicyRegistry {

    private final Map<String, ContributionPolicy> policies;

    public ContributionPolicyRegistry(Collection<? extends ContributionPolicy> policies) {
        Objects.requireNonNull(policies, "policies must not be null");

        this.policies = new HashMap<>();
        for (ContributionPolicy policy : policies) {
            Objects.requireNonNull(policy, "policy must not be null");
            String strategy =
                    Objects.requireNonNull(policy.strategy(), "strategy must not be null");
            if (this.policies.putIfAbsent(strategy, policy) != null) {
                throw new IllegalStateException("Duplicate contribution policy for " + strategy);
            }
        }
    }

    public ContributionPolicy get(String strategy) {
        var policy = policies.get(Objects.requireNonNull(strategy, "strategy must not be null"));
        if (policy == null) {
            throw new IllegalArgumentException("Unknown contribution strategy: " + strategy);
        }
        return policy;
    }
}
