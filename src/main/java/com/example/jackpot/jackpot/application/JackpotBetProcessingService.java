package com.example.jackpot.jackpot.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JackpotBetProcessingService {
    private final JackpotContributionService contributions;
    private final JackpotRewardEvaluationService rewards;

    public JackpotBetProcessingService(
            JackpotContributionService contributions, JackpotRewardEvaluationService rewards) {
        this.contributions = contributions;
        this.rewards = rewards;
    }

    @Transactional
    public RewardEvaluationResult process(ProcessBetContributionCommand command) {
        contributions.process(command);
        return rewards.evaluate(command.betId());
    }
}
