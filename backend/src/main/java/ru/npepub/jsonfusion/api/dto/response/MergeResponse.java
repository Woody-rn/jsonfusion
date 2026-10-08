package ru.npepub.jsonfusion.api.dto.response;

import ru.npepub.jsonfusion.domain.model.session.MergeSession;
import ru.npepub.jsonfusion.domain.model.result.MergeResult;

/**
 * Response for POST /api/sessions/{id}/merge.
 */
public record MergeResponse(
        String sessionId,
        String status,
        MergeSummary summary
) {

    public static MergeResponse from(MergeSession session) {
        MergeResult result = session.getResult();
        return new MergeResponse(
                session.getId(),
                session.getStatus().name(),
                MergeSummary.from(result)
        );
    }
}