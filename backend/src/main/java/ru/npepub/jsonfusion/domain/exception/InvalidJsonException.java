package ru.npepub.jsonfusion.domain.exception;

/**
 * Thrown when the uploaded file is not a valid JSON array of objects.
 */
public class InvalidJsonException extends RuntimeException {

    public InvalidJsonException(String message) {
        super(message);
    }

    public InvalidJsonException(String message, Throwable cause) {
        super(message, cause);
    }
}