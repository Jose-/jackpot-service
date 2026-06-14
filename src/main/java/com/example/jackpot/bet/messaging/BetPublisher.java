package com.example.jackpot.bet.messaging;

import com.example.jackpot.bet.application.PublishBetCommand;
import com.example.jackpot.jackpot.domain.BetStatus;

public interface BetPublisher {
    BetStatus publish(PublishBetCommand command);
}
