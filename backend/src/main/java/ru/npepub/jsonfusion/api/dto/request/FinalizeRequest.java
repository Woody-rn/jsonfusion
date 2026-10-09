package ru.npepub.jsonfusion.api.dto.request;

/**
 * Request body for POST /api/sessions/{id}/finalize.
 */
public record FinalizeRequest(
        boolean saveUnmatched
) {
}