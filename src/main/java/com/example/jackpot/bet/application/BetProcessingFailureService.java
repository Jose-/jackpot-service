package com.example.jackpot.bet.application;

import com.example.jackpot.bet.persistence.BetRepository;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BetProcessingFailureService {
    private final BetRepository bets;
    private final Clock clock;

    public BetProcessingFailureService(BetRepository bets, Clock clock) {
        this.bets = bets;
        this.clock = clock;
    }

    @Transactional
    public void record(UUID betId, Throwable failure) {
        bets.findByIdForUpdate(betId)
                .ifPresent(
                        bet ->
                                bet.markProcessingFailed(
                                        failure.getClass().getSimpleName(),
                                        safeMessage(failure),
                                        clock.instant()));
    }

    private String safeMessage(Throwable failure) {
        var message = failure.getMessage();
        if (message == null || message.isBlank()) {
            return "Bet processing failed";
        }
        return message.substring(0, Math.min(message.length(), 500));
    }
}
