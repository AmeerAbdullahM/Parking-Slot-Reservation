package com.parkpulse.parkingreservation.exception;

public class SlotConflictException extends RuntimeException {
    public SlotConflictException(String message) {
        super(message);
    }
}
