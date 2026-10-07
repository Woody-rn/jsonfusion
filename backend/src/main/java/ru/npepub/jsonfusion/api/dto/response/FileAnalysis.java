package ru.npepub.jsonfusion.api.dto.response;

import ru.npepub.jsonfusion.domain.model.JsonDocument;

import java.util.List;

/**
 * Summary of a parsed file: number of records and distinct field names.
 */
public record FileAnalysis(
        int recordsCount,
        List<String> fields
) {

    public static FileAnalysis from(JsonDocument document) {
        return new FileAnalysis(
                document.size(),
                List.copyOf(document.getFieldNames())
        );
    }
}