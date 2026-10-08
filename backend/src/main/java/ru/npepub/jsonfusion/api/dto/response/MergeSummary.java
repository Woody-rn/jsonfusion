package ru.npepub.jsonfusion.api.dto.response;

import ru.npepub.jsonfusion.domain.model.result.MergeResult;

/**
 * Summary counts of a merge result.
 */
public record MergeSummary(
        int matchedCount,
        int unmatchedLeftCount,
        int unmatchedRightCount
) {

    public static MergeSummary from(MergeResult result) {
        return new MergeSummary(
                result.merged().size(),
                result.unmatchedLeft().size(),
                result.unmatchedRight().size()
        );
    }
}