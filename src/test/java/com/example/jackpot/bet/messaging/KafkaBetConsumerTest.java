package com.example.jackpot.bet.messaging;

import static org.mockito.Mockito.verify;

import com.example.jackpot.jackpot.application.JackpotBetProcessingService;
import com.example.jackpot.jackpot.application.ProcessBetContributionCommand;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kafka bet consumer")
class KafkaBetConsumerTest {
    @Mock JackpotBetProcessingService processing;

    @Test
    @DisplayName("Should contribute and evaluate the bet when an event is consumed")
    void shouldContributeAndEvaluateBetWhenEventIsConsumed() {
        var event =
                new BetEvent(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        new BigDecimal("10.00"));

        new KafkaBetConsumer(processing).consume(event);

        verify(processing)
                .process(
                        new ProcessBetContributionCommand(
                                event.betId(), event.userId(), event.jackpotId(), event.amount()));
    }
}
