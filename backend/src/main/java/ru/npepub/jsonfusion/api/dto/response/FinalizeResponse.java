package ru.npepub.jsonfusion.api.dto.response;

import ru.npepub.jsonfusion.domain.model.result.Finalization;
import ru.npepub.jsonfusion.domain.model.session.MergeSession;
import ru.npepub.jsonfusion.domain.service.FinalizeOutcome;

/**
 * Response for POST /api/sessions/{id}/finalize.
 */
public record FinalizeResponse(
        String sessionId,
        String status,
        ResultSummary resultSummary,
        boolean unmatchedResultAvailable
) {

    public static FinalizeResponse from(MergeSession session, FinalizeOutcome outcome) {
        Finalization fin = outcome.finalization();
        ResultSummary summary = new ResultSummary(
                fin.finalResult().size(),
                outcome.fromMatched(),
                outcome.fromPairs(),
                outcome.unmatchedSaved(),
                outcome.ignoredCount()
        );
        return new FinalizeResponse(
                session.getId(),
                session.getStatus().name(),
                summary,
                fin.hasUnmatchedResult()
        );
    }
}