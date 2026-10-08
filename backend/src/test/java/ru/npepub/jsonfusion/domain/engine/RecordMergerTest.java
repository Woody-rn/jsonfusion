package ru.npepub.jsonfusion.domain.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;
import ru.npepub.jsonfusion.domain.model.config.ComparisonSettings;
import ru.npepub.jsonfusion.domain.model.config.FieldRole;
import ru.npepub.jsonfusion.domain.model.config.FieldRule;
import ru.npepub.jsonfusion.domain.model.json.JsonRecord;
import ru.npepub.jsonfusion.domain.model.config.MergeConfig;
import ru.npepub.jsonfusion.domain.model.config.PriorityMode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RecordMergerTest {

    private final FieldMerger fieldMerger = new FieldMerger();
    private final RecordMerger merger = new RecordMerger(fieldMerger);

    @Test
    void mergesMatchedRecordsOnly() {
        JsonRecord left1 = record("name", "A", "status", "blue");
        JsonRecord left2 = record("name", "B", "status", "red");
        JsonRecord right1 = record("name", "A", "leadId", "x");

        AnchorMatcher.GroupResult left = group(
                Map.of("a", left1, "b", left2),
                Map.of(),
                List.of()
        );
        AnchorMatcher.GroupResult right = group(
                Map.of("a", right1),
                Map.of(),
                List.of()
        );

        MergeConfig config = config(List.of(
                rule("name", FieldRole.ANCHOR),
                rule("status", FieldRole.PRIORITY_F1),
                rule("leadId", FieldRole.PRIORITY_F2)
        ));

        List<JsonRecord> result = merger.mergeMatched(left, right, config);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getField("name")).hasValue(TextNode.valueOf("A"));
        assertThat(result.getFirst().getField("status")).hasValue(TextNode.valueOf("blue"));
        assertThat(result.getFirst().getField("leadId")).hasValue(TextNode.valueOf("x"));
    }

    @Test
    void returnsEmptyWhenNoMatches() {
        JsonRecord left1 = record("name", "A");
        JsonRecord right1 = record("name", "B");

        AnchorMatcher.GroupResult left = group(Map.of("a", left1), Map.of(), List.of());
        AnchorMatcher.GroupResult right = group(Map.of("b", right1), Map.of(), List.of());

        MergeConfig config = config(List.of(rule("name", FieldRole.ANCHOR)));

        List<JsonRecord> result = merger.mergeMatched(left, right, config);

        assertThat(result).isEmpty();
    }

    @Test
    void assignsSourceIndexSequentially() {
        JsonRecord left1 = record("name", "A");
        JsonRecord left2 = record("name", "B");
        JsonRecord right1 = record("name", "A");
        JsonRecord right2 = record("name", "B");

        AnchorMatcher.GroupResult left = group(
                new LinkedHashMap<>(Map.of("a", left1, "b", left2)),
                Map.of(),
                List.of()
        );
        AnchorMatcher.GroupResult right = group(
                new LinkedHashMap<>(Map.of("a", right1, "b", right2)),
                Map.of(),
                List.of()
        );

        MergeConfig config = config(List.of(rule("name", FieldRole.ANCHOR)));

        List<JsonRecord> result = merger.mergeMatched(left, right, config);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).sourceIndex()).isZero();
        assertThat(result.get(1).sourceIndex()).isEqualTo(1);
    }

    private AnchorMatcher.GroupResult group(Map<String, JsonRecord> unique,
                                            Map<String, List<JsonRecord>> duplicates,
                                            List<JsonRecord> nullAnchors) {
        return new AnchorMatcher.GroupResult(unique, duplicates, nullAnchors);
    }

    private MergeConfig config(List<FieldRule> rules) {
        return new MergeConfig(rules, "name", new ComparisonSettings(true, true), List.of());
    }

    private FieldRule rule(String name, FieldRole role) {
        PriorityMode mode = (role == FieldRole.PRIORITY_F1 || role == FieldRole.PRIORITY_F2)
                ? PriorityMode.ALWAYS
                : null;
        return new FieldRule(name, role, mode, null, false);
    }

    private JsonRecord record(String... keyValues) {
        Map<String, JsonNode> fields = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            fields.put(keyValues[i], TextNode.valueOf(keyValues[i + 1]));
        }
        return new JsonRecord(fields, 0);
    }
}