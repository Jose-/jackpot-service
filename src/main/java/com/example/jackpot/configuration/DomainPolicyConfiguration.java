package com.example.jackpot.configuration;

import com.example.jackpot.jackpot.domain.policy.ContributionPolicy;
import com.example.jackpot.jackpot.domain.policy.ContributionPolicyRegistry;
import com.example.jackpot.jackpot.domain.policy.FixedContributionPolicy;
import com.example.jackpot.jackpot.domain.policy.RewardPolicy;
import com.example.jackpot.jackpot.domain.policy.RewardPolicyRegistry;
import com.example.jackpot.jackpot.domain.policy.VariableContributionPolicy;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainPolicyConfiguration {
    @Bean
    FixedContributionPolicy fixedContributionPolicy() {
        return new FixedContributionPolicy();
    }

    @Bean
    VariableContributionPolicy variableContributionPolicy() {
        return new VariableContributionPolicy();
    }

    @Bean
    ContributionPolicyRegistry contributionPolicyRegistry(List<ContributionPolicy> policies) {
        return new ContributionPolicyRegistry(policies);
    }

    @Bean
    RewardPolicyRegistry rewardPolicyRegistry(List<RewardPolicy> policies) {
        return new RewardPolicyRegistry(policies);
    }
}
