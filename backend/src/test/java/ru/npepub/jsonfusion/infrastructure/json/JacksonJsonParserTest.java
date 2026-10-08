package ru.npepub.jsonfusion.infrastructure.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import ru.npepub.jsonfusion.domain.exception.InvalidJsonException;
import ru.npepub.jsonfusion.domain.model.json.JsonDocument;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JacksonJsonParserTest {

    private final JacksonJsonParser parser = new JacksonJsonParser(new ObjectMapper());

    @Test
    void parsesValidArrayOfObjects() {
        JsonDocument doc = parse("""
                [
                  { "name": "A", "status": "blue" },
                  { "name": "B", "status": "red" }
                ]
                """);

        assertThat(doc.size()).isEqualTo(2);
        assertThat(doc.getFieldNames()).containsExactly("name", "status");
    }

    @Test
    void parsesEmptyArray() {
        JsonDocument doc = parse("[]");

        assertThat(doc.size()).isZero();
        assertThat(doc.getFieldNames()).isEmpty();
    }

    @Test
    void assignsSourceIndexInOrder() {
        JsonDocument doc = parse("""
                [ { "a": 1 }, { "a": 2 }, { "a": 3 } ]
                """);

        assertThat(doc.records()).extracting("sourceIndex").containsExactly(0, 1, 2);
    }

    @Test
    void preservesFieldOrderAsInFile() {
        JsonDocument doc = parse("""
                [ { "z": 1, "a": 2, "m": 3 } ]
                """);

        assertThat(doc.getFieldNames()).containsExactly("z", "a", "m");
    }

    @Test
    void rejectsNonArrayRoot() {
        assertThatThrownBy(() -> parse(
                """
                        { "a": 1 }
                        """))
                .isInstanceOf(InvalidJsonException.class)
                .hasMessageContaining("must be a JSON array");
    }

    @Test
    void rejectsArrayOfNonObjects() {
        assertThatThrownBy(() -> parse(
                """
                        [ 1, 2, 3 ]
                        """))
                .isInstanceOf(InvalidJsonException.class)
                .hasMessageContaining("must be a JSON object");
    }

    @Test
    void rejectsMalformedJson() {
        assertThatThrownBy(() -> parse("hello"))
                .isInstanceOf(InvalidJsonException.class)
                .hasMessageContaining("Failed to parse");
    }

    private JsonDocument parse(String json) {
        InputStream in = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        return parser.parse(in);
    }
}