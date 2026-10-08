package ru.npepub.jsonfusion.domain.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;
import ru.npepub.jsonfusion.domain.model.config.ComparisonSettings;
import ru.npepub.jsonfusion.domain.model.json.JsonDocument;
import ru.npepub.jsonfusion.domain.model.json.JsonRecord;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AnchorMatcherTest {

    private final JsonValueNormalizer normalizer = new JsonValueNormalizer();
    private final AnchorMatcher matcher = new AnchorMatcher(normalizer);

    private final ComparisonSettings lenient = new ComparisonSettings(true, true);
    private final ComparisonSettings strict = new ComparisonSettings(false, false);

    @Test
    void groupsRecordsWithDistinctAnchorsAsUnique() {
        JsonDocument doc = doc(
                record("name", "A"),
                record("name", "B"),
                record("name", "C")
        );

        AnchorMatcher.GroupResult result = matcher.group(doc, "name", lenient);

        assertThat(result.unique()).containsOnlyKeys("a", "b", "c");
        assertThat(result.duplicates()).isEmpty();
        assertThat(result.nullAnchors()).isEmpty();
    }

    @Test
    void putsRecordsWithSameAnchorIntoDuplicates() {
        JsonDocument doc = doc(
                record("name", "A"),
                record("name", "A")
        );

        AnchorMatcher.GroupResult result = matcher.group(doc, "name", lenient);

        assertThat(result.unique()).isEmpty();
        assertThat(result.duplicates()).containsOnlyKeys("a");
        assertThat(result.duplicates().get("a")).hasSize(2);
    }

    @Test
    void putsRecordsWithoutAnchorIntoNullAnchors() {
        JsonDocument doc = doc(
                record("status", "blue"),
                record("name", "A")
        );

        AnchorMatcher.GroupResult result = matcher.group(doc, "name", lenient);

        assertThat(result.unique()).containsOnlyKeys("a");
        assertThat(result.nullAnchors()).hasSize(1);
    }

    @Test
    void treatsNullAnchorValueAsNullAnchor() {
        JsonDocument doc = doc(
                recordWithNull("name"),
                record("name", "A")
        );

        AnchorMatcher.GroupResult result = matcher.group(doc, "name", lenient);

        assertThat(result.unique()).containsOnlyKeys("a");
        assertThat(result.nullAnchors()).hasSize(1);
    }

    @Test
    void normalizesAnchorValuesBeforeGrouping() {
        JsonDocument doc = doc(
                record("name", "ООО Ромашка"),
                record("name", "ООО  ромашка")
        );

        AnchorMatcher.GroupResult result = matcher.group(doc, "name", lenient);

        assertThat(result.duplicates()).containsOnlyKeys("ооо ромашка");
        assertThat(result.duplicates().get("ооо ромашка")).hasSize(2);
    }

    @Test
    void strictSettingsKeepAnchorsSeparate() {
        JsonDocument doc = doc(
                record("name", "ООО Ромашка"),
                record("name", "ООО ромашка")
        );

        AnchorMatcher.GroupResult result = matcher.group(doc, "name", strict);

        assertThat(result.unique()).containsOnlyKeys("ООО Ромашка", "ООО ромашка");
        assertThat(result.duplicates()).isEmpty();
    }

    private JsonDocument doc(JsonRecord... records) {
        return new JsonDocument(List.of(records));
    }

    private JsonRecord record(String key, String value) {
        Map<String, JsonNode> fields = new LinkedHashMap<>();
        fields.put(key, TextNode.valueOf(value));
        return new JsonRecord(fields, 0);
    }

    private JsonRecord recordWithNull(String key) {
        Map<String, JsonNode> fields = new LinkedHashMap<>();
        fields.put(key, com.fasterxml.jackson.databind.node.NullNode.getInstance());
        return new JsonRecord(fields, 0);
    }
}