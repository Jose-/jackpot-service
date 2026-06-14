package com.example.jackpot.bet.application;

import com.example.jackpot.bet.messaging.BetPublisher;
import com.example.jackpot.bet.persistence.BetEntity;
import com.example.jackpot.bet.persistence.BetRepository;
import com.example.jackpot.jackpot.persistence.JackpotRepository;
import com.example.jackpot.shared.error.BetPublicationException;
import com.example.jackpot.shared.error.ConflictingBetException;
import com.example.jackpot.shared.error.ResourceNotFoundException;
import java.time.Clock;
import java.util.Objects;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class PublishBetService {
    private final BetRepository repository;
    private final JackpotRepository jackpots;
    private final BetPublisher publisher;
    private final TransactionTemplate transactions;
    private final Clock clock;

    public PublishBetService(
            BetRepository repository,
            JackpotRepository jackpots,
            BetPublisher publisher,
            TransactionTemplate transactions,
            Clock clock) {
        this.repository = repository;
        this.jackpots = jackpots;
        this.publisher = publisher;
        this.transactions = transactions;
        this.clock = clock;
    }

    public PublishBetResult publish(PublishBetCommand command) {
        Objects.requireNonNull(command);
        boolean publish;
        try {
            publish = Boolean.TRUE.equals(transactions.execute(status -> register(command)));
        } catch (DataIntegrityViolationException duplicateRace) {
            publish =
                    Boolean.TRUE.equals(transactions.execute(status -> recoverDuplicate(command)));
        }
        if (!publish) return result(command.betId());
        try {
            var publicationStatus = publisher.publish(command);
            transactions.executeWithoutResult(
                    status ->
                            repository
                                    .findByIdForUpdate(command.betId())
                                    .orElseThrow()
                                    .markPublicationStatus(publicationStatus, clock.instant()));
        } catch (RuntimeException failure) {
            transactions.executeWithoutResult(
                    status ->
                            repository
                                    .findByIdForUpdate(command.betId())
                                    .orElseThrow()
                                    .markPublicationFailed(safeMessage(failure), clock.instant()));
            if (failure instanceof BetPublicationException publicationException)
                throw publicationException;
            throw new BetPublicationException("Unable to publish bet", failure);
        }
        return result(command.betId());
    }

    private boolean register(PublishBetCommand command) {
        if (!jackpots.existsById(command.jackpotId())) {
            throw new ResourceNotFoundException("Jackpot not found");
        }
        var existing = repository.findByIdForUpdate(command.betId());
        if (existing.isPresent()) {
            if (!existing.get()
                    .hasSamePayload(command.userId(), command.jackpotId(), command.amount()))
                throw new ConflictingBetException();
            return existing.get().prepareForPublication(clock.instant());
        }
        repository.saveAndFlush(
                new BetEntity(
                        command.betId(),
                        command.userId(),
                        command.jackpotId(),
                        command.amount(),
                        clock.instant()));
        return true;
    }

    private boolean recoverDuplicate(PublishBetCommand command) {
        var existing =
                repository
                        .findById(command.betId())
                        .orElseThrow(() -> new ConflictingBetException());
        if (!existing.hasSamePayload(command.userId(), command.jackpotId(), command.amount())) {
            throw new ConflictingBetException();
        }
        return false;
    }

    private PublishBetResult result(java.util.UUID id) {
        var bet = repository.findById(id).orElseThrow();
        return new PublishBetResult(id, bet.status());
    }

    private String safeMessage(RuntimeException failure) {
        var message = failure.getMessage();
        if (message == null || message.isBlank()) {
            return "Bet publication failed";
        }
        return message.substring(0, Math.min(message.length(), 500));
    }
}
