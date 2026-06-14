package com.example.jackpot.bet.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(
        name = "jackpot.messaging.mode",
        havingValue = "kafka",
        matchIfMissing = true)
public class KafkaTopicConfiguration {
    @Bean
    NewTopic jackpotBetsTopic(JackpotMessagingProperties properties) {
        return topic(properties.topic(), properties);
    }

    @Bean
    NewTopic jackpotBetsDeadLetterTopic(JackpotMessagingProperties properties) {
        return topic(properties.topic() + properties.dltSuffix(), properties);
    }

    private static NewTopic topic(String name, JackpotMessagingProperties properties) {
        return TopicBuilder.name(name)
                .partitions(properties.partitions())
                .replicas(properties.replicationFactor())
                .build();
    }
}
