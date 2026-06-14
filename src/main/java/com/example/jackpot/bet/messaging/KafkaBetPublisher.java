package com.example.jackpot.bet.messaging;

import com.example.jackpot.bet.application.PublishBetCommand;
import com.example.jackpot.jackpot.domain.BetStatus;
import com.example.jackpot.shared.error.BetPublicationException;
import java.util.concurrent.CompletionException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "jackpot.messaging.mode",
        havingValue = "kafka",
        matchIfMissing = true)
public class KafkaBetPublisher implements BetPublisher {
    private final KafkaTemplate<String, BetEvent> kafka;
    private final JackpotMessagingProperties properties;

    public KafkaBetPublisher(
            KafkaTemplate<String, BetEvent> kafka, JackpotMessagingProperties properties) {
        this.kafka = kafka;
        this.properties = properties;
    }

    @Override
    public BetStatus publish(PublishBetCommand command) {
        try {
            kafka.send(properties.topic(), command.jackpotId().toString(), BetEvent.from(command))
                    .join();
            return BetStatus.PUBLISHED;
        } catch (CompletionException exception) {
            throw new BetPublicationException(
                    "Unable to publish bet to Kafka", exception.getCause());
        }
    }
}
