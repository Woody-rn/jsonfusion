package ru.npepub.jsonfusion.domain.exception;

/**
 * Thrown when a session with the given id does not exist.
 */
public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException(String sessionId) {
        super("Session not found: " + sessionId);
    }
}