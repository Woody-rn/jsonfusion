package ru.npepub.jsonfusion.api.dto.response;

import ru.npepub.jsonfusion.api.dto.AnchorSettingsDto;
import ru.npepub.jsonfusion.api.dto.NewFieldDto;
import ru.npepub.jsonfusion.domain.model.config.FieldRole;
import ru.npepub.jsonfusion.domain.model.config.FieldRule;
import ru.npepub.jsonfusion.domain.model.json.JsonDocument;
import ru.npepub.jsonfusion.domain.model.config.MergeConfig;

import java.util.List;
import java.util.Set;

/**
 * Response for GET /fields and PUT /config.
 */
public record FieldsResponse(
        List<FieldInfo> fields,
        AnchorSettingsDto anchorSettings,
        List<NewFieldDto> newFields
) {

    public static FieldsResponse from(MergeConfig config,
                                      JsonDocument file1,
                                      JsonDocument file2) {
        Set<String> names1 = file1.getFieldNames();
        Set<String> names2 = file2.getFieldNames();

        List<FieldInfo> fields = config.fieldRules().stream()
                .map(rule -> toFieldInfo(rule, names1, names2, config.anchorField()))
                .toList();

        List<NewFieldDto> newFields = config.newFields().stream()
                .map(NewFieldDto::from)
                .toList();

        return new FieldsResponse(fields, AnchorSettingsDto.from(config), newFields);
    }

    private static FieldInfo toFieldInfo(FieldRule rule,
                                         Set<String> names1,
                                         Set<String> names2,
                                         String anchorField) {
        boolean in1 = names1.contains(rule.fieldName());
        boolean in2 = names2.contains(rule.fieldName());
        FieldRole defaultRole = computeDefaultRole(rule.fieldName(), in1, anchorField);
        return FieldInfo.from(rule, in1, in2, defaultRole);
    }

    private static FieldRole computeDefaultRole(String name, boolean inFile1, String anchorField) {
        if (name.equals(anchorField)) {
            return FieldRole.ANCHOR;
        }
        return inFile1 ? FieldRole.PRIORITY_F1 : FieldRole.PRIORITY_F2;
    }
}