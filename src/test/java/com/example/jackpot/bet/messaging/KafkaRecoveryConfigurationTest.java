package com.example.jackpot.bet.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;

@SpringBootTest(properties = "jackpot.messaging.mode=log")
@DisplayName("Kafka recovery configuration")
class KafkaRecoveryConfigurationTest {
    @Autowired CommonErrorHandler errorHandler;
    @Autowired JackpotMessagingProperties properties;

    @Test
    @DisplayName("Should configure bounded retries before recovering a failed Kafka record")
    void shouldConfigureBoundedRetriesBeforeRecoveringFailedKafkaRecord() {
        assertThat(errorHandler).isInstanceOf(DefaultErrorHandler.class);
        assertThat(properties.retryIntervalMs()).isEqualTo(1_000);
        assertThat(properties.maxRetries()).isEqualTo(3);
    }
}
