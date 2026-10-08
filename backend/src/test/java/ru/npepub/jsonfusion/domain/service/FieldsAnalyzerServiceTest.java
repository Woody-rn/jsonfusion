package ru.npepub.jsonfusion.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;
import ru.npepub.jsonfusion.domain.exception.InvalidSessionStateException;
import ru.npepub.jsonfusion.domain.model.config.FieldRole;
import ru.npepub.jsonfusion.domain.model.config.FieldRule;
import ru.npepub.jsonfusion.domain.model.json.JsonDocument;
import ru.npepub.jsonfusion.domain.model.json.JsonRecord;
import ru.npepub.jsonfusion.domain.model.config.MergeConfig;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FieldsAnalyzerServiceTest {

    private final FieldsAnalyzerService service = new FieldsAnalyzerService();

    @Test
    void anchorsOnNameFieldWhenPresentInBothFiles() {
        JsonDocument file1 = doc(record("name", "A"), record("name", "B"));
        JsonDocument file2 = doc(record("name", "C"));

        MergeConfig config = service.analyze(file1, file2);

        assertThat(config.anchorField()).isEqualTo("name");
    }

    @Test
    void defaultsCommonFieldsToPriorityF1() {
        JsonDocument file1 = doc(record("name", "A", "status", "blue"));
        JsonDocument file2 = doc(record("name", "A", "status", "red"));

        MergeConfig config = service.analyze(file1, file2);

        FieldRule status = config.findRule("status").orElseThrow();
        assertThat(status.role()).isEqualTo(FieldRole.PRIORITY_F1);
    }

    @Test
    void defaultsFieldsOnlyInFile2ToPriorityF2() {
        JsonDocument file1 = doc(record("name", "A"));
        JsonDocument file2 = doc(record("name", "A", "leadId", "x"));

        MergeConfig config = service.analyze(file1, file2);

        FieldRule leadId = config.findRule("leadId").orElseThrow();
        assertThat(leadId.role()).isEqualTo(FieldRole.PRIORITY_F2);
    }

    @Test
    void picksFirstFieldAsAnchorWhenNameIsAbsent() {
        JsonDocument file1 = doc(record("email", "a@b.c"));
        JsonDocument file2 = doc(record("email", "x@y.z"));

        MergeConfig config = service.analyze(file1, file2);

        assertThat(config.anchorField()).isEqualTo("email");
    }

    @Test
    void rejectsEmptyDocuments() {
        JsonDocument file1 = doc();
        JsonDocument file2 = doc();

        assertThatThrownBy(() -> service.analyze(file1, file2))
                .isInstanceOf(InvalidSessionStateException.class);
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
}