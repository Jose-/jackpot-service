package com.example.jackpot.jackpot.application;

import com.example.jackpot.bet.persistence.BetRepository;
import com.example.jackpot.jackpot.domain.Bet;
import com.example.jackpot.jackpot.domain.JackpotContribution;
import com.example.jackpot.jackpot.domain.policy.ContributionPolicyRegistry;
import com.example.jackpot.jackpot.persistence.JackpotContributionEntity;
import com.example.jackpot.jackpot.persistence.JackpotContributionRepository;
import com.example.jackpot.jackpot.persistence.JackpotRepository;
import com.example.jackpot.shared.error.ConflictingBetException;
import com.example.jackpot.shared.error.ResourceNotFoundException;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JackpotContributionService {
    private final BetRepository bets;
    private final JackpotContributionRepository contributions;
    private final JackpotRepository jackpots;
    private final ContributionPolicyRegistry policies;
    private final Clock clock;

    public JackpotContributionService(
            BetRepository bets,
            JackpotContributionRepository contributions,
            JackpotRepository jackpots,
            ContributionPolicyRegistry policies,
            Clock clock) {
        this.bets = bets;
        this.contributions = contributions;
        this.jackpots = jackpots;
        this.policies = policies;
        this.clock = clock;
    }

    @Transactional
    public JackpotContribution process(ProcessBetContributionCommand command) {
        var bet =
                bets.findByIdForUpdate(command.betId())
                        .orElseThrow(() -> new ResourceNotFoundException("Bet not found"));
        if (!bet.hasSamePayload(command.userId(), command.jackpotId(), command.amount()))
            throw new ConflictingBetException();
        var existing = contributions.findByBetId(command.betId());
        if (existing.isPresent()) return existing.get().toDomain();
        var jackpotEntity =
                jackpots.findByIdForUpdate(command.jackpotId())
                        .orElseThrow(() -> new ResourceNotFoundException("Jackpot not found"));
        var jackpot = jackpotEntity.toDomain();
        var contribution =
                jackpot.contribute(
                        new Bet(
                                bet.id(),
                                bet.userId(),
                                bet.jackpotId(),
                                bet.amount(),
                                bet.createdAt()),
                        policies.get(jackpot.contributionConfiguration().strategy()),
                        clock.instant());
        jackpotEntity.apply(jackpot);
        contributions.save(new JackpotContributionEntity(contribution));
        bet.markContributed(clock.instant());
        return contribution;
    }
}
