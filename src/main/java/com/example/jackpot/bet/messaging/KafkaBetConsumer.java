package com.example.jackpot.bet.messaging;

import com.example.jackpot.jackpot.application.JackpotBetProcessingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "jackpot.messaging.mode",
        havingValue = "kafka",
        matchIfMissing = true)
public class KafkaBetConsumer {
    private final JackpotBetProcessingService processing;

    public KafkaBetConsumer(JackpotBetProcessingService processing) {
        this.processing = processing;
    }

    @KafkaListener(
            topics = "${jackpot.messaging.topic}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consume(BetEvent event) {
        processing.process(event.toContributionCommand());
    }
}
