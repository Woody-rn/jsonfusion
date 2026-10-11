package ru.npepub.jsonfusion.api.dto.response;

import ru.npepub.jsonfusion.domain.model.session.MergeSession;

import java.util.List;

/**
 * Response for GET /unmatched and PUT /unmatched/decisions.
 */
public record UnmatchedResponse(
        String sessionId,
        String status,
        List<UnmatchedRecordDto> unmatchedLeft,
        List<UnmatchedRecordDto> unmatchedRight
) {

    public static UnmatchedResponse of(MergeSession session,
                                       List<UnmatchedRecordDto> left,
                                       List<UnmatchedRecordDto> right) {
        return new UnmatchedResponse(
                session.getId(),
                session.getStatus().name(),
                left,
                right
        );
    }
}