package com.example.jackpot.configuration;

import com.example.jackpot.bet.application.PublishBetCommand;
import com.example.jackpot.bet.messaging.BetEvent;
import com.example.jackpot.bet.messaging.BetPublisher;
import com.example.jackpot.jackpot.domain.BetStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "jackpot.messaging.mode", havingValue = "log")
public class LoggingBetPublisher implements BetPublisher {
    private static final Logger log = LoggerFactory.getLogger(LoggingBetPublisher.class);

    @Override
    public BetStatus publish(PublishBetCommand command) {
        log.info("Simulated jackpot bet publication: {}", BetEvent.from(command));
        return BetStatus.PUBLISHED;
    }
}
