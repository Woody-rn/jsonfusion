package ru.npepub.jsonfusion.api.dto.response;

import ru.npepub.jsonfusion.domain.model.config.FieldRole;
import ru.npepub.jsonfusion.domain.model.config.FieldRule;
import ru.npepub.jsonfusion.domain.model.config.PriorityMode;

/**
 * Info about a single field in the merge configuration.
 */
public record FieldInfo(
        String name,
        boolean presentInFile1,
        boolean presentInFile2,
        FieldRole defaultRole,
        FieldRole role,
        PriorityMode priorityMode,
        String ifEqualsValue,
        boolean ifEqualsNull
) {

    public static FieldInfo from(FieldRule rule, boolean inFile1, boolean inFile2, FieldRole defaultRole) {
        return new FieldInfo(
                rule.fieldName(),
                inFile1,
                inFile2,
                defaultRole,
                rule.role(),
                rule.priorityMode(),
                rule.ifEqualsValue(),
                rule.ifEqualsNull()
        );
    }
}