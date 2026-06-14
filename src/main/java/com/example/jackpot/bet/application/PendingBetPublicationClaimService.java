package com.example.jackpot.bet.application;

import com.example.jackpot.bet.persistence.BetRepository;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PendingBetPublicationClaimService {
    private final BetRepository bets;
    private final Clock clock;

    public PendingBetPublicationClaimService(BetRepository bets, Clock clock) {
        this.bets = bets;
        this.clock = clock;
    }

    @Transactional
    public List<PublishBetCommand> claimStale() {
        var now = clock.instant();
        var staleBefore = now.minus(Duration.ofMillis(30_000));
        return bets.findRecoverableForUpdate(staleBefore, PageRequest.of(0, 100)).stream()
                .map(
                        bet -> {
                            bet.claimPublicationRecovery(now);
                            return new PublishBetCommand(
                                    bet.id(), bet.userId(), bet.jackpotId(), bet.amount());
                        })
                .toList();
    }
}
