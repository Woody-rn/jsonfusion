package ru.npepub.jsonfusion.domain.service;

import ru.npepub.jsonfusion.domain.model.result.Finalization;

/**
 * Result of finalization: the {@link Finalization} itself plus summary counters
 * describing how the final document was assembled.
 */
public record FinalizeOutcome(
        Finalization finalization,
        int fromMatched,
        int fromPairs,
        int unmatchedSaved,
        int ignoredCount
) {
}