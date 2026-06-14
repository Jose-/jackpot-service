package com.example.jackpot.jackpot.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jackpot.bet.persistence.BetEntity;
import com.example.jackpot.bet.persistence.BetRepository;
import com.example.jackpot.configuration.JackpotSeedProperties;
import com.example.jackpot.jackpot.persistence.JackpotContributionRepository;
import com.example.jackpot.jackpot.persistence.JackpotRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "jackpot.messaging.mode=log")
@DisplayName("Concurrent jackpot contribution processing")
class ConcurrentContributionIntegrationTest {
    @Autowired JackpotContributionService service;
    @Autowired BetRepository bets;
    @Autowired JackpotContributionRepository contributions;
    @Autowired JackpotRepository jackpots;
    @Autowired JackpotSeedProperties seedProperties;

    @Test
    @DisplayName("Should preserve one fractional contribution when the same bet is processed again")
    void shouldPreserveOneFractionalContributionWhenSameBetIsProcessedAgain() {
        var betId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var amount = new BigDecimal("0.01");
        bets.saveAndFlush(
                new BetEntity(betId, userId, seedProperties.fixedId(), amount, Instant.now()));
        var command =
                new ProcessBetContributionCommand(betId, userId, seedProperties.fixedId(), amount);
        var poolBefore = jackpots.findById(seedProperties.fixedId()).orElseThrow().currentPool();

        var first = service.process(command);
        var second = service.process(command);
        var persisted = contributions.findByBetId(betId).orElseThrow().toDomain();
        var poolAfter = jackpots.findById(seedProperties.fixedId()).orElseThrow().currentPool();

        assertThat(first.contributionAmount()).isEqualTo(new BigDecimal("0.00050000"));
        assertThat(second.contributionAmount()).isEqualTo(first.contributionAmount());
        assertThat(persisted.contributionAmount()).isEqualTo(new BigDecimal("0.00050000"));
        assertThat(persisted.currentJackpotAmount().scale()).isEqualTo(8);
        assertThat(poolAfter.subtract(poolBefore)).isEqualTo(new BigDecimal("0.00050000"));
        assertThat(poolAfter.scale()).isEqualTo(8);
        assertThat(
                        contributions.findAll().stream()
                                .filter(contribution -> contribution.betId().equals(betId)))
                .hasSize(1);
    }

    @Test
    @DisplayName("Should persist one contribution when the same bet is processed concurrently")
    void shouldPersistOneContributionWhenSameBetIsProcessedConcurrently() throws Exception {
        var betId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var amount = new BigDecimal("20.00");
        bets.saveAndFlush(
                new BetEntity(betId, userId, seedProperties.fixedId(), amount, Instant.now()));
        var command =
                new ProcessBetContributionCommand(betId, userId, seedProperties.fixedId(), amount);
        var start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(2)) {
            var first =
                    executor.submit(
                            () -> {
                                start.await();
                                return service.process(command);
                            });
            var second =
                    executor.submit(
                            () -> {
                                start.await();
                                return service.process(command);
                            });

            start.countDown();
            assertThat(first.get())
                    .usingRecursiveComparison()
                    .ignoringFields("createdAt")
                    .isEqualTo(second.get());
        }

        assertThat(contributions.findByBetId(betId)).isPresent();
        assertThat(
                        contributions.findAll().stream()
                                .filter(contribution -> contribution.betId().equals(betId)))
                .hasSize(1);
    }
}
