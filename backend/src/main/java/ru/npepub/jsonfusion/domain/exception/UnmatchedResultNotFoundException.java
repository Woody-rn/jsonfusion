package ru.npepub.jsonfusion.domain.exception;

/**
 * Thrown when the unmatched result is not available for a finalized session
 * (either not saved, or there were no unmatched records).
 */
public class UnmatchedResultNotFoundException extends RuntimeException {

    public UnmatchedResultNotFoundException(String sessionId) {
        super("Unmatched result not available for session: " + sessionId);
    }
}