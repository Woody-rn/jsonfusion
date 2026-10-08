package ru.npepub.jsonfusion.domain.model.unmatched;

import java.util.List;
import java.util.Set;

/**
 * User decisions about unmatched records: manual pairs and ignored ids.
 */
public record UnmatchedDecision(
        List<UnmatchedPair> pairs,
        Set<String> ignoredIds
) {

    public UnmatchedDecision {
        pairs = List.copyOf(pairs);
        ignoredIds = Set.copyOf(ignoredIds);
    }
}