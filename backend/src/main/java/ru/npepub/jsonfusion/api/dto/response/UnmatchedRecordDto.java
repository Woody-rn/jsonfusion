package ru.npepub.jsonfusion.api.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedRecord;

import java.util.Map;

/**
 * A single unmatched record in the response, with optional label and ignored flag
 * applied from the user decision.
 */
public record UnmatchedRecordDto(
        String id,
        Map<String, JsonNode> record,
        String type,
        String label,
        boolean ignored
) {

    public static UnmatchedRecordDto from(UnmatchedRecord source, String label, boolean ignored) {
        return new UnmatchedRecordDto(
                source.id(),
                source.record().fields(),
                source.type().name(),
                label,
                ignored
        );
    }
}