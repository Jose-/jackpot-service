package com.example.jackpot.jackpot.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jackpot.JackpotApplication;
import com.example.jackpot.bet.persistence.BetEntity;
import com.example.jackpot.bet.persistence.BetRepository;
import com.example.jackpot.configuration.JackpotSeedProperties;
import com.example.jackpot.jackpot.persistence.JackpotRewardEvaluationRepository;
import com.example.jackpot.jackpot.persistence.JackpotRewardRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootTest(
        properties = "jackpot.messaging.mode=log",
        classes = {
            JackpotApplication.class,
            ConcurrentRewardEvaluationIntegrationTest.DrawConfiguration.class
        })
@DisplayName("Concurrent reward evaluation")
class ConcurrentRewardEvaluationIntegrationTest {
    @Autowired JackpotRewardEvaluationService service;
    @Autowired JackpotContributionService contributionService;
    @Autowired BetRepository bets;
    @Autowired JackpotRewardEvaluationRepository evaluations;
    @Autowired JackpotRewardRepository rewards;
    @Autowired JackpotSeedProperties seedProperties;

    @Test
    @DisplayName(
            "Should persist one winning evaluation and one reward when the same bet is evaluated concurrently")
    void shouldPersistOneWinningEvaluationAndOneRewardWhenSameBetIsEvaluatedConcurrently()
            throws Exception {
        var betId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var amount = new BigDecimal("20.00");
        bets.saveAndFlush(
                new BetEntity(betId, userId, seedProperties.fixedId(), amount, Instant.now()));
        contributionService.process(
                new ProcessBetContributionCommand(betId, userId, seedProperties.fixedId(), amount));
        var start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(2)) {
            var first =
                    executor.submit(
                            () -> {
                                start.await();
                                return service.evaluate(betId);
                            });
            var second =
                    executor.submit(
                            () -> {
                                start.await();
                                return service.evaluate(betId);
                            });

            start.countDown();
            assertThat(first.get())
                    .usingRecursiveComparison()
                    .ignoringFields("createdAt")
                    .isEqualTo(second.get());
        }

        assertThat(
                        evaluations.findAll().stream()
                                .filter(evaluation -> evaluation.betId().equals(betId)))
                .hasSize(1);
        assertThat(rewards.findByBetId(betId)).isPresent();
    }

    @TestConfiguration
    static class DrawConfiguration {
        @Bean
        @Primary
        DrawGenerator winningDrawGenerator() {
            return () -> BigDecimal.ZERO;
        }
    }
}
