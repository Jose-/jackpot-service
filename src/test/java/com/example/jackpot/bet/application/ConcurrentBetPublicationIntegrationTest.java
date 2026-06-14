package com.example.jackpot.bet.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jackpot.JackpotApplication;
import com.example.jackpot.bet.messaging.BetPublisher;
import com.example.jackpot.bet.persistence.BetRepository;
import com.example.jackpot.configuration.JackpotSeedProperties;
import com.example.jackpot.jackpot.domain.BetStatus;
import com.example.jackpot.shared.error.ConflictingBetException;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
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
            ConcurrentBetPublicationIntegrationTest.RecordingPublisherConfiguration.class
        })
@DisplayName("Concurrent bet publication")
class ConcurrentBetPublicationIntegrationTest {
    @Autowired PublishBetService service;
    @Autowired BetRepository bets;
    @Autowired JackpotSeedProperties jackpots;
    @Autowired RecordingPublisher publisher;

    @BeforeEach
    void clearPublisher() {
        publisher.commands.clear();
    }

    @Test
    @DisplayName(
            "Should publish only once and return success for concurrent identical publications")
    void shouldPublishOnlyOnceAndReturnSuccessForConcurrentIdenticalPublications()
            throws Exception {
        var command = command(UUID.randomUUID(), new BigDecimal("10.00"));

        var results = publishConcurrently(command, command);

        assertThat(results)
                .allSatisfy(result -> assertThat(result).isInstanceOf(PublishBetResult.class));
        assertThat(bets.findAll().stream().filter(bet -> bet.id().equals(command.betId())))
                .hasSize(1);
        assertThat(publisher.commands).hasSize(1);
    }

    @Test
    @DisplayName("Should return conflict and publish once for concurrent different payloads")
    void shouldReturnConflictAndPublishOnceForConcurrentDifferentPayloads() throws Exception {
        var betId = UUID.randomUUID();

        var results =
                publishConcurrently(
                        command(betId, new BigDecimal("10.00")),
                        command(betId, new BigDecimal("20.00")));

        assertThat(results)
                .anySatisfy(result -> assertThat(result).isInstanceOf(PublishBetResult.class));
        assertThat(results)
                .anySatisfy(
                        result -> assertThat(result).isInstanceOf(ConflictingBetException.class));
        assertThat(publisher.commands).hasSize(1);
    }

    private java.util.List<Object> publishConcurrently(
            PublishBetCommand first, PublishBetCommand second) throws Exception {
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Future<Object> firstResult = executor.submit(() -> publishAfter(start, first));
            Future<Object> secondResult = executor.submit(() -> publishAfter(start, second));
            start.countDown();
            return java.util.List.of(firstResult.get(), secondResult.get());
        }
    }

    private Object publishAfter(CountDownLatch start, PublishBetCommand command)
            throws InterruptedException {
        start.await();
        try {
            return service.publish(command);
        } catch (RuntimeException exception) {
            return exception;
        }
    }

    private PublishBetCommand command(UUID betId, BigDecimal amount) {
        return new PublishBetCommand(betId, UUID.randomUUID(), jackpots.fixedId(), amount);
    }

    @TestConfiguration
    static class RecordingPublisherConfiguration {
        @Bean
        @Primary
        RecordingPublisher recordingPublisher() {
            return new RecordingPublisher();
        }
    }

    static class RecordingPublisher implements BetPublisher {
        private final ConcurrentLinkedQueue<PublishBetCommand> commands =
                new ConcurrentLinkedQueue<>();

        @Override
        public BetStatus publish(PublishBetCommand command) {
            commands.add(command);
            return BetStatus.PUBLISHED;
        }
    }
}
