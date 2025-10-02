package com.couriertracking.exception;

// Custom exception for invalid location data such as out-of-bounds coordinates
public class InvalidLocationException extends RuntimeException {
    public InvalidLocationException(String message) {
        super(message);
    }
}