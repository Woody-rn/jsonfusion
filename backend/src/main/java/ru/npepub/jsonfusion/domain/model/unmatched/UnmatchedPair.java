package ru.npepub.jsonfusion.domain.model.unmatched;

/**
 * A manually matched pair of unmatched records (left from file1, right from file2).
 */
public record UnmatchedPair(
        String leftId,
        String rightId,
        String label
) {
}