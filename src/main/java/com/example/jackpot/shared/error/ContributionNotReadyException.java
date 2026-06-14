package com.example.jackpot.shared.error;

public class ContributionNotReadyException extends RuntimeException {
    public ContributionNotReadyException(String message) {
        super(message);
    }
}
