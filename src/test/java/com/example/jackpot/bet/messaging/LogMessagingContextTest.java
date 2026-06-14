package com.example.jackpot.bet.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jackpot.configuration.LoggingBetPublisher;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("log")
@DisplayName("Log messaging application context")
class LogMessagingContextTest {
    @Autowired Map<String, BetPublisher> publishers;

    @Autowired(required = false)
    Map<String, NewTopic> topics = Map.of();

    @Test
    @DisplayName("Should select only the logging publisher without provisioning Kafka topics")
    void shouldSelectOnlyLoggingPublisherWithoutProvisioningKafkaTopics() {
        assertThat(publishers).hasSize(1);
        assertThat(publishers.values().iterator().next()).isInstanceOf(LoggingBetPublisher.class);
        assertThat(topics).isEmpty();
    }
}
