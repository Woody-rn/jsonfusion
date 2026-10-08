package ru.npepub.jsonfusion.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import ru.npepub.jsonfusion.domain.model.NewField;

/**
 * A field to add to every result record.
 */
public record NewFieldDto(
        String name,
        JsonNode defaultValue
) {

    public static NewFieldDto from(NewField field) {
        return new NewFieldDto(field.name(), field.defaultValue());
    }

    public NewField toDomain() {
        return new NewField(name, defaultValue);
    }
}