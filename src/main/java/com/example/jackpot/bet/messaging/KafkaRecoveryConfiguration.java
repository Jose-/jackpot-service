package com.example.jackpot.bet.messaging;

import com.example.jackpot.bet.application.BetProcessingFailureService;
import com.example.jackpot.shared.error.ConflictingBetException;
import com.example.jackpot.shared.error.ResourceNotFoundException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaRecoveryConfiguration {
    @Bean
    CommonErrorHandler jackpotKafkaErrorHandler(
            JackpotMessagingProperties properties,
            KafkaTemplate<String, BetEvent> kafka,
            BetProcessingFailureService failures) {
        var deadLetter =
                new DeadLetterPublishingRecoverer(
                        kafka,
                        (record, exception) ->
                                new TopicPartition(
                                        record.topic() + properties.dltSuffix(),
                                        record.partition()));
        var handler =
                new DefaultErrorHandler(
                        (record, exception) -> {
                            deadLetter.accept(record, exception);
                            if (record.value() instanceof BetEvent event) {
                                failures.record(event.betId(), exception);
                            }
                        },
                        new FixedBackOff(properties.retryIntervalMs(), properties.maxRetries()));
        handler.addNotRetryableExceptions(
                ResourceNotFoundException.class,
                ConflictingBetException.class,
                IllegalArgumentException.class);
        handler.addRetryableExceptions(TransientDataAccessException.class);
        return handler;
    }
}
