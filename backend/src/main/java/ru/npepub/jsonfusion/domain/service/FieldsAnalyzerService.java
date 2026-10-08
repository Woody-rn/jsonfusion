package ru.npepub.jsonfusion.domain.service;

import org.springframework.stereotype.Service;
import ru.npepub.jsonfusion.domain.exception.InvalidSessionStateException;
import ru.npepub.jsonfusion.domain.model.ComparisonSettings;
import ru.npepub.jsonfusion.domain.model.FieldRole;
import ru.npepub.jsonfusion.domain.model.FieldRule;
import ru.npepub.jsonfusion.domain.model.JsonDocument;
import ru.npepub.jsonfusion.domain.model.MergeConfig;
import ru.npepub.jsonfusion.domain.model.PriorityMode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds a default {@link MergeConfig} from two parsed documents.
 * Fields present in both documents default to PRIORITY_F1.
 * Fields present only in file2 default to PRIORITY_F2.
 * The first common field becomes the default anchor.
 */
@Service
public class FieldsAnalyzerService {

    private static final String DEFAULT_ANCHOR_FIELD = "name";

    public MergeConfig analyze(JsonDocument file1, JsonDocument file2) {
        Set<String> fields1 = file1.getFieldNames();
        Set<String> fields2 = file2.getFieldNames();

        Set<String> unionFields = new LinkedHashSet<>();
        unionFields.addAll(fields1);
        unionFields.addAll(fields2);

        if (unionFields.isEmpty()) {
            throw new InvalidSessionStateException("Cannot analyze files: no fields found");
        }

        String anchor = pickAnchor(unionFields);

        List<FieldRule> rules = new ArrayList<>();
        for (String fieldName : unionFields) {
            rules.add(defaultRule(fieldName, fields1, anchor));
        }

        ComparisonSettings settings = new ComparisonSettings(true, true);
        return new MergeConfig(rules, anchor, settings, List.of());
    }

    private String pickAnchor(Set<String> fields) {
        if (fields.contains(DEFAULT_ANCHOR_FIELD)) {
            return DEFAULT_ANCHOR_FIELD;
        }
        return fields.iterator().next();
    }

    private FieldRule defaultRule(String fieldName, Set<String> fields1, String anchor) {
        if (fieldName.equals(anchor)) {
            return new FieldRule(fieldName, FieldRole.ANCHOR, null, null, false);
        }

        FieldRole role = fields1.contains(fieldName)
                ? FieldRole.PRIORITY_F1
                : FieldRole.PRIORITY_F2;

        return new FieldRule(fieldName, role, PriorityMode.ALWAYS, null, false);
    }
}