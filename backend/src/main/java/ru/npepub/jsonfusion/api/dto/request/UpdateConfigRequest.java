package ru.npepub.jsonfusion.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import ru.npepub.jsonfusion.api.dto.AnchorSettingsDto;
import ru.npepub.jsonfusion.api.dto.NewFieldDto;

import java.util.List;

/**
 * Request body for PUT /api/sessions/{id}/config.
 */
public record UpdateConfigRequest(

        @NotEmpty(message = "fields must not be empty")
        @Valid
        List<FieldRuleDto> fields,

        @NotNull(message = "anchorSettings is required")
        @Valid
        AnchorSettingsDto anchorSettings,

        @NotNull(message = "newFields must not be null")
        @Valid
        List<NewFieldDto> newFields
) {
}