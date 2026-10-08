package ru.npepub.jsonfusion.domain.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;
import ru.npepub.jsonfusion.domain.model.FieldRole;
import ru.npepub.jsonfusion.domain.model.FieldRule;
import ru.npepub.jsonfusion.domain.model.JsonRecord;
import ru.npepub.jsonfusion.domain.model.PriorityMode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FieldMergerTest {

    private final FieldMerger merger = new FieldMerger();

    @Test
    void takesAnchorValueFromLeft() {
        JsonRecord left = record("name", "left-name");
        JsonRecord right = record("name", "right-name");

        JsonRecord result = merger.merge(left, right,
                List.of(rule("name", FieldRole.ANCHOR)),
                0);

        assertThat(result.getField("name")).hasValue(TextNode.valueOf("left-name"));
    }

    @Test
    void overwritesWithPriorityF1WhenAlways() {
        JsonRecord left = record("status", "blue");
        JsonRecord right = record("status", "red");

        JsonRecord result = merger.merge(left, right,
                List.of(rule("status", FieldRole.PRIORITY_F1)),
                0);

        assertThat(result.getField("status")).hasValue(TextNode.valueOf("blue"));
    }

    @Test
    void overwritesWithPriorityF2WhenAlways() {
        JsonRecord left = record("status", "blue");
        JsonRecord right = record("status", "red");

        JsonRecord result = merger.merge(left, right,
                List.of(rule("status", FieldRole.PRIORITY_F2)),
                0);

        assertThat(result.getField("status")).hasValue(TextNode.valueOf("red"));
    }

    @Test
    void fillsOnlyWhenOtherIsNullAndIfEqualsNull() {
        JsonRecord left = record("status", "blue");
        JsonRecord right = recordWithNull("status");

        JsonRecord result = merger.merge(left, right,
                List.of(ruleIfEqualsNull("status", FieldRole.PRIORITY_F1)),
                0);

        assertThat(result.getField("status")).hasValue(TextNode.valueOf("blue"));
    }

    @Test
    void keepsOtherWhenConditionNotMet() {
        JsonRecord left = record("status", "blue");
        JsonRecord right = record("status", "red");

        JsonRecord result = merger.merge(left, right,
                List.of(ruleIfEqualsNull("status", FieldRole.PRIORITY_F1)),
                0);

        assertThat(result.getField("status")).hasValue(TextNode.valueOf("red"));
    }

    @Test
    void fillsWhenValueEqualsCaseInsensitive() {
        JsonRecord left = record("status", "blue");
        JsonRecord right = record("status", "UNKNOWN");

        JsonRecord result = merger.merge(left, right,
                List.of(ruleIfEquals("status", FieldRole.PRIORITY_F1, "unknown")),
                0);

        assertThat(result.getField("status")).hasValue(TextNode.valueOf("blue"));
    }

    @Test
    void skipsDeleteFields() {
        JsonRecord left = record("status", "blue");
        JsonRecord right = record("status", "red");

        JsonRecord result = merger.merge(left, right,
                List.of(rule("status", FieldRole.DELETE)),
                0);

        assertThat(result.getField("status")).isEmpty();
    }

    @Test
    void usesOtherValueWhenPriorityIsMissing() {
        JsonRecord left = record("status", "blue");
        JsonRecord right = record("leadId", "x");

        JsonRecord result = merger.merge(left, right,
                List.of(rule("leadId", FieldRole.PRIORITY_F1)),
                0);

        assertThat(result.getField("leadId")).hasValue(TextNode.valueOf("x"));
    }

    private FieldRule rule(String name, FieldRole role) {
        PriorityMode mode = isPriority(role) ? PriorityMode.ALWAYS : null;
        return new FieldRule(name, role, mode, null, false);
    }

    private FieldRule ruleIfEqualsNull(String name, FieldRole role) {
        return new FieldRule(name, role, PriorityMode.IF_EQUALS, null, true);
    }

    private FieldRule ruleIfEquals(String name, FieldRole role, String value) {
        return new FieldRule(name, role, PriorityMode.IF_EQUALS, value, false);
    }

    private boolean isPriority(FieldRole role) {
        return role == FieldRole.PRIORITY_F1 || role == FieldRole.PRIORITY_F2;
    }

    private JsonRecord record(String key, String value) {
        Map<String, JsonNode> fields = new LinkedHashMap<>();
        fields.put(key, TextNode.valueOf(value));
        return new JsonRecord(fields, 0);
    }

    private JsonRecord recordWithNull(String key) {
        Map<String, JsonNode> fields = new LinkedHashMap<>();
        fields.put(key, NullNode.getInstance());
        return new JsonRecord(fields, 0);
    }
}