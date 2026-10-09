package ru.npepub.jsonfusion.api.dto.response;

/**
 * Counts of a finalized result.
 */
public record ResultSummary(
        int totalRecords,
        int fromMatched,
        int fromPairs,
        int unmatchedSaved,
        int ignoredCount
) {
}