package ru.npepub.jsonfusion.domain.model;

/**
 * Lifecycle status of a merge session. Determines which operations are allowed.
 */
public enum SessionStatus {
    CREATED,
    FILES_UPLOADED,
    CONFIGURED,
    MERGED,
    UNMATCHED_RESOLVED,
    FINALIZED
}