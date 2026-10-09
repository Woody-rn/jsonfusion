package ru.npepub.jsonfusion.domain.model.result;

import ru.npepub.jsonfusion.domain.model.json.JsonDocument;

/**
 * Final result of a session: the main merged document and optionally
 * a document with unresolved unmatched records.
 */
public record Finalization(
        JsonDocument finalResult,
        JsonDocument unmatchedResult
) {

    public boolean hasUnmatchedResult() {
        return unmatchedResult != null;
    }
}