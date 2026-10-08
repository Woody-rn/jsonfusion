package ru.npepub.jsonfusion.api.dto.response;

import java.util.List;

/**
 * Response for GET /unmatched and PUT /unmatched/decisions.
 */
public record UnmatchedResponse(
        List<UnmatchedRecordDto> unmatchedLeft,
        List<UnmatchedRecordDto> unmatchedRight
) {
}