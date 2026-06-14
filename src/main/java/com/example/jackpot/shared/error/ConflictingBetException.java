package com.example.jackpot.shared.error;

public class ConflictingBetException extends RuntimeException {
    public ConflictingBetException() {
        super("Bet ID already exists with different data");
    }
}
