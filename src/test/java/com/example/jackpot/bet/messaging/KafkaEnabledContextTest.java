package com.example.jackpot.bet.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        properties = {
            "spring.kafka.listener.auto-startup=false",
            "spring.kafka.admin.auto-create=false"
        })
@DisplayName("Kafka-enabled application context")
class KafkaEnabledContextTest {
    @Autowired Map<String, BetPublisher> publishers;
    @Autowired Map<String, NewTopic> topics;

    @Test
    @DisplayName("Should select only the Kafka publisher when messaging is enabled")
    void shouldSelectOnlyKafkaPublisherWhenMessagingIsEnabled() {
        assertThat(publishers).hasSize(1);
        assertThat(publishers.values().iterator().next()).isInstanceOf(KafkaBetPublisher.class);
    }

    @Test
    @DisplayName("Should provision source and dead-letter topics with matching partitions")
    void shouldProvisionSourceAndDeadLetterTopicsWithMatchingPartitions() {
        assertThat(topics).containsOnlyKeys("jackpotBetsTopic", "jackpotBetsDeadLetterTopic");
        assertThat(topics.get("jackpotBetsTopic").name()).isEqualTo("jackpot-bets");
        assertThat(topics.get("jackpotBetsDeadLetterTopic").name()).isEqualTo("jackpot-bets.DLT");
        assertThat(topics.get("jackpotBetsTopic").numPartitions())
                .isEqualTo(topics.get("jackpotBetsDeadLetterTopic").numPartitions())
                .isEqualTo(3);
    }
}
