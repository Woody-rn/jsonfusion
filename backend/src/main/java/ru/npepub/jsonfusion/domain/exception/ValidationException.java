package ru.npepub.jsonfusion.domain.exception;

/**
 * Thrown when the domain validation of a request fails.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}