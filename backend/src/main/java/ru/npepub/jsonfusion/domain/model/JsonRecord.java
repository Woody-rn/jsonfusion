package ru.npepub.jsonfusion.domain.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A single record from a JSON document. Fields are stored as-is
 * (original {@link JsonNode} values) to preserve type information.
 */
public record JsonRecord(Map<String, JsonNode> fields, int sourceIndex) {

    public Optional<JsonNode> getField(String name) {
        return Optional.ofNullable(fields.get(name));
    }

    public boolean hasField(String name) {
        return fields.containsKey(name);
    }

    public Set<String> getFieldNames() {
        return fields.keySet();
    }
}