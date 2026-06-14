package com.example.jackpot.bet.application;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "jackpot.messaging.mode",
        havingValue = "kafka",
        matchIfMissing = true)
public class PendingBetPublicationRecovery {
    private final PendingBetPublicationClaimService claims;
    private final PublishBetService publications;

    public PendingBetPublicationRecovery(
            PendingBetPublicationClaimService claims, PublishBetService publications) {
        this.claims = claims;
        this.publications = publications;
    }

    @Scheduled(fixedDelayString = "${jackpot.messaging.recovery-interval-ms}")
    public void recover() {
        claims.claimStale().forEach(publications::publish);
    }
}
