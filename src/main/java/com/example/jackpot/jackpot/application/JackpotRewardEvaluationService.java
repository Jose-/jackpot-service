package com.example.jackpot.jackpot.application;

import com.example.jackpot.bet.persistence.BetRepository;
import com.example.jackpot.jackpot.domain.JackpotReward;
import com.example.jackpot.jackpot.domain.policy.RewardEvaluationContext;
import com.example.jackpot.jackpot.domain.policy.RewardPolicyRegistry;
import com.example.jackpot.jackpot.persistence.JackpotContributionRepository;
import com.example.jackpot.jackpot.persistence.JackpotRepository;
import com.example.jackpot.jackpot.persistence.JackpotRewardEntity;
import com.example.jackpot.jackpot.persistence.JackpotRewardEvaluationEntity;
import com.example.jackpot.jackpot.persistence.JackpotRewardEvaluationRepository;
import com.example.jackpot.jackpot.persistence.JackpotRewardRepository;
import com.example.jackpot.shared.error.ContributionNotReadyException;
import com.example.jackpot.shared.error.ResourceNotFoundException;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JackpotRewardEvaluationService {
    private final BetRepository bets;
    private final JackpotContributionRepository contributions;
    private final JackpotRewardEvaluationRepository evaluations;
    private final JackpotRewardRepository rewards;
    private final JackpotRepository jackpots;
    private final RewardPolicyRegistry policies;
    private final DrawGenerator drawGenerator;
    private final Clock clock;

    public JackpotRewardEvaluationService(
            BetRepository bets,
            JackpotContributionRepository contributions,
            JackpotRewardEvaluationRepository evaluations,
            JackpotRewardRepository rewards,
            JackpotRepository jackpots,
            RewardPolicyRegistry policies,
            DrawGenerator drawGenerator,
            Clock clock) {
        this.bets = bets;
        this.contributions = contributions;
        this.evaluations = evaluations;
        this.rewards = rewards;
        this.jackpots = jackpots;
        this.policies = policies;
        this.drawGenerator = drawGenerator;
        this.clock = clock;
    }

    @Transactional
    public RewardEvaluationResult evaluate(java.util.UUID betId) {
        var bet =
                bets.findByIdForUpdate(betId)
                        .orElseThrow(() -> new ResourceNotFoundException("Bet not found"));
        var contribution =
                contributions
                        .findByBetId(betId)
                        .orElseThrow(
                                () ->
                                        new ContributionNotReadyException(
                                                "Bet has not contributed to a jackpot yet"));
        var existing = evaluations.findByBetId(betId);
        if (existing.isPresent()) {
            return toResult(existing.get());
        }

        var jackpotEntity =
                jackpots.findByIdForUpdate(contribution.jackpotId())
                        .orElseThrow(() -> new ResourceNotFoundException("Jackpot not found"));
        var jackpot = jackpotEntity.toDomain();
        var policy = policies.get(jackpot.rewardConfiguration().strategy());
        var decision =
                policy.evaluate(
                        new RewardEvaluationContext(
                                jackpot, bet.amount(), drawGenerator.generate()));
        var now = clock.instant();
        java.math.BigDecimal rewardAmount = null;
        if (decision.won()) {
            rewardAmount = jackpot.rewardAndReset(now);
            rewards.save(
                    new JackpotRewardEntity(
                            new JackpotReward(
                                    bet.id(), bet.userId(), jackpot.id(), rewardAmount, now)));
            jackpotEntity.apply(jackpot);
        }

        var evaluation =
                evaluations.save(
                        new JackpotRewardEvaluationEntity(
                                bet.id(),
                                bet.userId(),
                                jackpot.id(),
                                decision.won(),
                                decision.calculatedChance(),
                                decision.generatedDraw(),
                                rewardAmount,
                                now));
        return toResult(evaluation);
    }

    private RewardEvaluationResult toResult(JackpotRewardEvaluationEntity evaluation) {
        return new RewardEvaluationResult(
                evaluation.betId(),
                evaluation.userId(),
                evaluation.jackpotId(),
                evaluation.won(),
                evaluation.calculatedChance(),
                evaluation.generatedDraw(),
                evaluation.rewardAmount(),
                evaluation.createdAt());
    }
}
