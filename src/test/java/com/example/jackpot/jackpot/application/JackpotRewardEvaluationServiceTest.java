package com.example.jackpot.jackpot.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jackpot.JackpotApplication;
import com.example.jackpot.bet.persistence.BetEntity;
import com.example.jackpot.bet.persistence.BetRepository;
import com.example.jackpot.configuration.JackpotSeedProperties;
import com.example.jackpot.jackpot.persistence.JackpotContributionRepository;
import com.example.jackpot.jackpot.persistence.JackpotRepository;
import com.example.jackpot.jackpot.persistence.JackpotRewardEvaluationRepository;
import com.example.jackpot.jackpot.persistence.JackpotRewardRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
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
            JackpotRewardEvaluationServiceTest.DrawConfiguration.class
        })
@DisplayName("Jackpot reward evaluation service")
class JackpotRewardEvaluationServiceTest {
    @Autowired JackpotRewardEvaluationService service;
    @Autowired JackpotContributionService contributionService;
    @Autowired BetRepository bets;
    @Autowired JackpotContributionRepository contributions;
    @Autowired JackpotRewardEvaluationRepository evaluations;
    @Autowired JackpotRewardRepository rewards;
    @Autowired JackpotRepository jackpots;
    @Autowired JackpotSeedProperties seedProperties;
    @Autowired AdjustableDrawGenerator drawGenerator;

    @Test
    @DisplayName(
            "Should persist and return the original evaluation when the same bet is evaluated again")
    void shouldPersistAndReturnOriginalEvaluationWhenSameBetIsEvaluatedAgain() {
        var betId = createContributedBet(new BigDecimal("0.01"));
        drawGenerator.set(new BigDecimal("99.0000"));

        var first = service.evaluate(betId);
        drawGenerator.set(BigDecimal.ZERO);
        var second = service.evaluate(betId);

        assertThat(first).usingRecursiveComparison().ignoringFields("createdAt").isEqualTo(second);
        assertThat(first.won()).isFalse();
        assertThat(evaluations.findByBetId(betId)).isPresent();
        assertThat(rewards.findByBetId(betId)).isEmpty();
    }

    @Test
    @DisplayName("Should persist a reward and reset the jackpot when the generated draw wins")
    void shouldPersistRewardAndResetJackpotWhenGeneratedDrawWins() {
        var betId = createContributedBet(new BigDecimal("20.00"));
        var poolBeforeEvaluation =
                jackpots.findById(seedProperties.fixedId()).orElseThrow().currentPool();
        drawGenerator.set(BigDecimal.ZERO);

        var result = service.evaluate(betId);

        assertThat(result.won()).isTrue();
        assertThat(result.rewardAmount()).isEqualTo(poolBeforeEvaluation);
        assertThat(result.rewardAmount().scale()).isEqualTo(8);
        assertThat(rewards.findByBetId(betId)).isPresent();
        assertThat(jackpots.findById(seedProperties.fixedId()).orElseThrow().currentPool())
                .isEqualTo(new BigDecimal("1000.00000000"));
    }

    private UUID createContributedBet(BigDecimal amount) {
        return createContributedBet(seedProperties.fixedId(), amount);
    }

    private UUID createContributedBet(UUID jackpotId, BigDecimal amount) {
        var betId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        bets.saveAndFlush(new BetEntity(betId, userId, jackpotId, amount, Instant.now()));
        contributionService.process(
                new ProcessBetContributionCommand(betId, userId, jackpotId, amount));
        return betId;
    }

    @TestConfiguration
    static class DrawConfiguration {
        @Bean
        @Primary
        AdjustableDrawGenerator adjustableDrawGenerator() {
            return new AdjustableDrawGenerator();
        }
    }

    static class AdjustableDrawGenerator implements DrawGenerator {
        private final AtomicReference<BigDecimal> draw =
                new AtomicReference<>(new BigDecimal("99.0000"));

        void set(BigDecimal value) {
            draw.set(value);
        }

        @Override
        public BigDecimal generate() {
            return draw.get();
        }
    }
}
