package ru.npepub.jsonfusion.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedPair;

/**
 * A pair of unmatched ids from the client's decision request.
 */
public record UnmatchedPairDto(

        @NotBlank(message = "leftId must not be blank")
        String leftId,

        @NotBlank(message = "rightId must not be blank")
        String rightId,

        String label
) {

    public UnmatchedPair toDomain() {
        return new UnmatchedPair(leftId, rightId, label);
    }
}