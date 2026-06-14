package com.example.jackpot.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "jackpot.messaging.mode=log")
@ActiveProfiles("dev")
@DisplayName("H2 console development profile")
class H2ConsoleDevProfileTest {
    @Autowired ApplicationContext context;
    @Autowired Environment environment;
    @Autowired KafkaProperties kafkaProperties;

    @Test
    @DisplayName("Should enable the H2 console when the dev profile is active")
    void shouldEnableH2ConsoleWhenDevProfileIsActive() {
        assertThat(environment.getProperty("spring.h2.console.enabled", Boolean.class)).isTrue();
        assertThat(environment.getProperty("spring.h2.console.path")).isEqualTo("/h2-console");
        assertThat(context.containsBean("h2Console")).isTrue();
    }

    @Test
    @DisplayName("Should reduce Kafka producer timeout when the dev profile is active")
    void shouldReduceKafkaProducerTimeoutWhenDevProfileIsActive() {
        assertThat(kafkaProperties.getProducer().getProperties())
                .containsEntry("max.block.ms", "1000");
    }
}
