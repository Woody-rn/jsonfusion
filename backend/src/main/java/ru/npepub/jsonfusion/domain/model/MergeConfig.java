package ru.npepub.jsonfusion.domain.model;

import java.util.List;
import java.util.Optional;

/**
 * Full merge configuration: field rules, anchor, comparison settings, added fields.
 */
public record MergeConfig(
        List<FieldRule> fieldRules,
        String anchorField,
        ComparisonSettings comparisonSettings,
        List<NewField> newFields
) {

    public MergeConfig {
        fieldRules = List.copyOf(fieldRules);
        newFields = List.copyOf(newFields);
    }

    public Optional<FieldRule> findRule(String fieldName) {
        return fieldRules.stream()
                .filter(r -> r.fieldName().equals(fieldName))
                .findFirst();
    }
}