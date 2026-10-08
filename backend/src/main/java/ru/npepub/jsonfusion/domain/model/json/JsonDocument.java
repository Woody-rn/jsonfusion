package ru.npepub.jsonfusion.domain.model.json;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A parsed JSON document: an ordered list of records.
 */
public record JsonDocument(
        List<JsonRecord> records
) {
    public JsonDocument {
        records = List.copyOf(records);
    }

    public int size() {
        return records.size();
    }

    /**
     * Returns the distinct field names across all records, in order of first appearance.
     */
    public Set<String> getFieldNames() {
        return records.stream()
                .flatMap(r -> r.getFieldNames().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}