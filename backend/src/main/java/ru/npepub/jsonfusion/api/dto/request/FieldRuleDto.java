package ru.npepub.jsonfusion.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.npepub.jsonfusion.domain.model.FieldRole;
import ru.npepub.jsonfusion.domain.model.FieldRule;
import ru.npepub.jsonfusion.domain.model.PriorityMode;

/**
 * A single field rule in the config update request.
 */
public record FieldRuleDto(

        @NotBlank(message = "field name must not be blank")
        String name,

        @NotNull(message = "role is required")
        FieldRole role,

        PriorityMode priorityMode,

        String ifEqualsValue,

        boolean ifEqualsNull
) {

    public FieldRule toDomain() {
        return new FieldRule(name, role, priorityMode, ifEqualsValue, ifEqualsNull);
    }
}