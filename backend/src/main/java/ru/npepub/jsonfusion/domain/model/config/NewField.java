package ru.npepub.jsonfusion.domain.model.config;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A field added to every result record with a constant default value.
 */
public record NewField(
        String name,
        JsonNode defaultValue
) {
}