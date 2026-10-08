package ru.npepub.jsonfusion.domain.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;
import ru.npepub.jsonfusion.domain.model.config.*;
import ru.npepub.jsonfusion.domain.model.json.JsonDocument;
import ru.npepub.jsonfusion.domain.model.json.JsonRecord;
import ru.npepub.jsonfusion.domain.model.result.MergeResult;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MergeEngineTest {

    private final JsonValueNormalizer normalizer = new JsonValueNormalizer();
    private final AnchorMatcher matcher = new AnchorMatcher(normalizer);
    private final FieldMerger fieldMerger = new FieldMerger();
    private final RecordMerger recordMerger = new RecordMerger(fieldMerger);
    private final MergeEngine engine = new MergeEngine(matcher, recordMerger);

    private final ComparisonSettings settings = new ComparisonSettings(true, true);

    @Test
    void mergesMatchedRecordsAndCollectsUnmatched() {
        JsonDocument file1 = doc(
                record("name", "A", "status", "blue"),
                record("name", "B", "status", "red")
        );
        JsonDocument file2 = doc(
                record("name", "A", "status", "red"),
                record("name", "C", "status", "green")
        );

        MergeResult result = engine.merge(file1, file2, config());

        assertThat(result.merged()).hasSize(1);
        assertThat(result.merged().getFirst().getField("name")).hasValue(TextNode.valueOf("A"));

        assertThat(result.unmatchedLeft()).hasSize(1);
        assertThat(result.unmatchedLeft().getFirst().type()).isEqualTo(UnmatchedType.ONLY_IN_LEFT);
        assertThat(result.unmatchedLeft().getFirst().id()).isEqualTo("L1");

        assertThat(result.unmatchedRight()).hasSize(1);
        assertThat(result.unmatchedRight().getFirst().type()).isEqualTo(UnmatchedType.ONLY_IN_RIGHT);
        assertThat(result.unmatchedRight().getFirst().id()).isEqualTo("R1");
    }

    @Test
    void putsDuplicateAnchorsAsUnmatched() {
        JsonDocument file1 = doc(
                record("name", "A", "status", "blue"),
                record("name", "A", "status", "red")
        );
        JsonDocument file2 = doc(
                record("name", "A", "status", "green")
        );

        MergeResult result = engine.merge(file1, file2, config());

        assertThat(result.merged()).isEmpty();
        assertThat(result.unmatchedLeft()).hasSize(2);
        assertThat(result.unmatchedLeft()).allMatch(r -> r.type() == UnmatchedType.DUPLICATE);
        assertThat(result.unmatchedRight()).hasSize(1);
        assertThat(result.unmatchedRight().getFirst().type()).isEqualTo(UnmatchedType.ONLY_IN_RIGHT);
    }

    @Test
    void nullAnchorRecordsBecomeUnmatched() {
        JsonDocument file1 = doc(
                record("name", "A", "status", "blue"),
                recordWithoutName("status", "red")
        );
        JsonDocument file2 = doc(
                record("name", "A", "status", "green")
        );

        MergeResult result = engine.merge(file1, file2, config());

        assertThat(result.merged()).hasSize(1);
        assertThat(result.unmatchedLeft()).hasSize(1);
        assertThat(result.unmatchedLeft().getFirst().type()).isEqualTo(UnmatchedType.ONLY_IN_LEFT);
    }

    @Test
    void assignsIdsSequentiallyPerSide() {
        JsonDocument file1 = doc(
                record("name", "A"),
                record("name", "B"),
                record("name", "C")
        );
        JsonDocument file2 = doc(
                record("name", "X"),
                record("name", "Y")
        );

        MergeResult result = engine.merge(file1, file2, config());

        assertThat(result.unmatchedLeft()).extracting("id")
                .containsExactly("L1", "L2", "L3");
        assertThat(result.unmatchedRight()).extracting("id")
                .containsExactly("R1", "R2");
    }

    private MergeConfig config() {
        return new MergeConfig(
                List.of(
                        rule("name", FieldRole.ANCHOR),
                        rule("status", FieldRole.PRIORITY_F1)
                ),
                "name",
                settings,
                List.of()
        );
    }

    private FieldRule rule(String name, FieldRole role) {
        PriorityMode mode = (role == FieldRole.PRIORITY_F1 || role == FieldRole.PRIORITY_F2)
                ? PriorityMode.ALWAYS
                : null;
        return new FieldRule(name, role, mode, null, false);
    }

    private JsonDocument doc(JsonRecord... records) {
        return new JsonDocument(List.of(records));
    }

    private JsonRecord record(String... keyValues) {
        Map<String, JsonNode> fields = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            fields.put(keyValues[i], TextNode.valueOf(keyValues[i + 1]));
        }
        return new JsonRecord(fields, 0);
    }

    private JsonRecord recordWithoutName(String... keyValues) {
        Map<String, JsonNode> fields = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            fields.put(keyValues[i], TextNode.valueOf(keyValues[i + 1]));
        }
        return new JsonRecord(fields, 0);
    }
}