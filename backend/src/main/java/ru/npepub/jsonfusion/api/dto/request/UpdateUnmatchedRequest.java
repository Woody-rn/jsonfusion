package ru.npepub.jsonfusion.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedDecision;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedPair;

import java.util.List;
import java.util.Set;

/**
 * Request body for PUT /api/sessions/{id}/unmatched/decisions.
 */
public record UpdateUnmatchedRequest(

        @NotNull(message = "pairs must not be null")
        @Valid
        List<UnmatchedPairDto> pairs,

        @NotNull(message = "ignoredIds must not be null")
        Set<String> ignoredIds
) {

    public UnmatchedDecision toDomain() {
        List<UnmatchedPair> domainPairs = pairs.stream()
                .map(UnmatchedPairDto::toDomain)
                .toList();
        return new UnmatchedDecision(domainPairs, ignoredIds);
    }
}