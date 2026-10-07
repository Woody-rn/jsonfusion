package ru.npepub.jsonfusion.domain.exception;

/**
 * Thrown when an operation is not allowed in the current session state.
 */
public class InvalidSessionStateException extends RuntimeException {

    public InvalidSessionStateException(String message) {
        super(message);
    }
}