package com.example.jackpot.bet.messaging;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jackpot.bet.application.PublishBetCommand;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kafka bet publisher")
class KafkaBetPublisherTest {
    @Mock KafkaTemplate<String, BetEvent> kafka;

    @Test
    @DisplayName("Should use the jackpot identifier as the Kafka record key")
    void shouldUseJackpotIdentifierAsKafkaRecordKey() {
        var properties = new JackpotMessagingProperties();
        var command =
                new PublishBetCommand(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        new BigDecimal("10.00"));
        var event = BetEvent.from(command);
        when(kafka.send(properties.topic(), command.jackpotId().toString(), event))
                .thenReturn(CompletableFuture.completedFuture(new SendResult<>(null, null)));

        new KafkaBetPublisher(kafka, properties).publish(command);

        verify(kafka).send(properties.topic(), command.jackpotId().toString(), event);
    }
}
