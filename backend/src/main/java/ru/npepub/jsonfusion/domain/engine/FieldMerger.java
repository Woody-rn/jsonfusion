package ru.npepub.jsonfusion.domain.engine;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import ru.npepub.jsonfusion.domain.model.config.FieldRole;
import ru.npepub.jsonfusion.domain.model.config.FieldRule;
import ru.npepub.jsonfusion.domain.model.json.JsonRecord;
import ru.npepub.jsonfusion.domain.model.config.PriorityMode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Applies field rules to merge two matched records into one.
 * The anchor field value is always taken from the left record.
 */
@Component
public class FieldMerger {

    /**
     * Merges {@code left} and {@code right} using the given rules.
     * The result contains only fields referenced by rules with roles other than DELETE.
     */
    public JsonRecord merge(JsonRecord left,
                            JsonRecord right,
                            List<FieldRule> rules,
                            int sourceIndex) {
        Map<String, JsonNode> result = new LinkedHashMap<>();

        for (FieldRule rule : rules) {
            if (rule.role() == FieldRole.DELETE) {
                continue;
            }

            if (rule.role() == FieldRole.ANCHOR) {
                left.getField(rule.fieldName()).ifPresent(v -> result.put(rule.fieldName(), v));
                continue;
            }

            JsonNode value = pickValue(left, right, rule);
            if (value != null) {
                result.put(rule.fieldName(), value);
            }
        }

        return new JsonRecord(result, sourceIndex);
    }

    private JsonNode pickValue(JsonRecord left, JsonRecord right, FieldRule rule) {
        JsonRecord priority = rule.role() == FieldRole.PRIORITY_F1 ? left : right;
        JsonRecord other = rule.role() == FieldRole.PRIORITY_F1 ? right : left;

        JsonNode priorityValue = priority.getField(rule.fieldName()).orElse(null);
        JsonNode otherValue = other.getField(rule.fieldName()).orElse(null);

        if (rule.priorityMode() == PriorityMode.IF_EQUALS
                && !matchesCondition(otherValue, rule)) {
            return otherValue;
        }

        return priorityValue != null ? priorityValue : otherValue;
    }

    private boolean matchesCondition(JsonNode currentValue, FieldRule rule) {
        if (rule.ifEqualsNull() || rule.ifEqualsValue() == null) {
            return currentValue == null || currentValue.isNull();
        }
        if (currentValue == null || currentValue.isNull()) {
            return false;
        }
        return currentValue.asText().equalsIgnoreCase(rule.ifEqualsValue());
    }
}