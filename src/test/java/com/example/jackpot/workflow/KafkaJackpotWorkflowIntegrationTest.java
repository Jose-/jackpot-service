package com.example.jackpot.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.jackpot.bet.application.PendingBetPublicationRecovery;
import com.example.jackpot.bet.messaging.BetEvent;
import com.example.jackpot.bet.persistence.BetEntity;
import com.example.jackpot.bet.persistence.BetRepository;
import com.example.jackpot.configuration.JackpotSeedProperties;
import com.example.jackpot.jackpot.persistence.JackpotContributionRepository;
import com.example.jackpot.jackpot.persistence.JackpotRewardEvaluationRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "jackpot.messaging.mode=kafka")
@AutoConfigureMockMvc
@EmbeddedKafka(
        partitions = 1,
        topics = {"jackpot-bets", "jackpot-bets.DLT"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@DisplayName("Real Kafka jackpot workflow")
class KafkaJackpotWorkflowIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JackpotSeedProperties jackpots;
    @Autowired JackpotContributionRepository contributions;
    @Autowired JackpotRewardEvaluationRepository evaluations;
    @Autowired BetRepository bets;
    @Autowired KafkaTemplate<String, BetEvent> kafka;
    @Autowired EmbeddedKafkaBroker broker;
    @Autowired PendingBetPublicationRecovery recovery;

    @Test
    @DisplayName("Should publish, consume, contribute, and evaluate exactly once through Kafka")
    void shouldPublishConsumeContributeAndEvaluateExactlyOnceThroughKafka() throws Exception {
        var betId = UUID.randomUUID();
        var request =
                """
			{"betId":"%s","userId":"%s","jackpotId":"%s","betAmount":10.00}
			"""
                        .formatted(betId, UUID.randomUUID(), jackpots.fixedId());

        mvc.perform(post("/api/v1/bets").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isAccepted());
        awaitEvaluation(betId);

        mvc.perform(post("/api/v1/bets").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isAccepted());

        assertThat(contributions.findByBetId(betId)).isPresent();
        assertThat(evaluations.findByBetId(betId)).isPresent();
    }

    @Test
    @DisplayName("Should route a permanently invalid external event to the dead-letter topic")
    void shouldRoutePermanentlyInvalidExternalEventToDeadLetterTopic() {
        var event =
                new BetEvent(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        new BigDecimal("10.00"));

        try (Consumer<String, String> dltConsumer = dltConsumer()) {
            broker.consumeFromAnEmbeddedTopic(dltConsumer, "jackpot-bets.DLT");
            kafka.send("jackpot-bets", event.jackpotId().toString(), event).join();

            var record =
                    KafkaTestUtils.getSingleRecord(
                            dltConsumer, "jackpot-bets.DLT", Duration.ofSeconds(10));

            assertThat(record.value()).contains(event.betId().toString());
            assertThat(contributions.findByBetId(event.betId())).isEmpty();
            assertThat(evaluations.findByBetId(event.betId())).isEmpty();
        }
    }

    @Test
    @DisplayName("Should recover a stale pending publication")
    void shouldRecoverStalePendingPublication() throws InterruptedException {
        var betId = UUID.randomUUID();
        bets.saveAndFlush(
                new BetEntity(
                        betId,
                        UUID.randomUUID(),
                        jackpots.fixedId(),
                        new BigDecimal("10.00"),
                        Instant.now().minus(Duration.ofMinutes(1))));

        recovery.recover();

        awaitEvaluation(betId);
        assertThat(contributions.findByBetId(betId)).isPresent();
    }

    private void awaitEvaluation(UUID betId) throws InterruptedException {
        var deadline = Instant.now().plus(Duration.ofSeconds(10));
        while (Instant.now().isBefore(deadline) && evaluations.findByBetId(betId).isEmpty()) {
            Thread.sleep(50);
        }
        assertThat(evaluations.findByBetId(betId)).isPresent();
    }

    private Consumer<String, String> dltConsumer() {
        Map<String, Object> properties =
                KafkaTestUtils.consumerProps("dlt-test-" + UUID.randomUUID(), "false", broker);
        return new DefaultKafkaConsumerFactory<>(
                        properties, new StringDeserializer(), new StringDeserializer())
                .createConsumer();
    }
}
