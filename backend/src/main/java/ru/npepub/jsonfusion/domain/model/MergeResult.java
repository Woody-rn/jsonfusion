package ru.npepub.jsonfusion.domain.model;

import java.util.List;

/**
 * Result of a merge operation: matched records and unmatched records from both sides.
 */
public record MergeResult(
        List<JsonRecord> merged,
        List<UnmatchedRecord> unmatchedLeft,
        List<UnmatchedRecord> unmatchedRight
) {

    public MergeResult {
        merged = List.copyOf(merged);
        unmatchedLeft = List.copyOf(unmatchedLeft);
        unmatchedRight = List.copyOf(unmatchedRight);
    }
}