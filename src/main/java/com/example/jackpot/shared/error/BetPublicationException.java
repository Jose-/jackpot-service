package com.example.jackpot.shared.error;

public class BetPublicationException extends RuntimeException {
    public BetPublicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BetPublicationException(String message) {
        super(message);
    }
}
