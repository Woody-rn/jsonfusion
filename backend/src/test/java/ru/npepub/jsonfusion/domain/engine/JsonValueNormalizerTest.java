package ru.npepub.jsonfusion.domain.engine;

import com.fasterxml.jackson.databind.node.*;
import org.junit.jupiter.api.Test;
import ru.npepub.jsonfusion.domain.model.ComparisonSettings;

import static org.assertj.core.api.Assertions.assertThat;

class JsonValueNormalizerTest {

    private final JsonValueNormalizer normalizer = new JsonValueNormalizer();

    private final ComparisonSettings strict = new ComparisonSettings(false, false);
    private final ComparisonSettings lenient = new ComparisonSettings(true, true);

    @Test
    void returnsNullForNullNode() {
        assertThat(normalizer.normalize(NullNode.getInstance(), lenient)).isNull();
    }

    @Test
    void returnsNullForNullValue() {
        assertThat(normalizer.normalize(null, lenient)).isNull();
    }

    @Test
    void returnsTextAsIsWhenNoNormalization() {
        assertThat(normalizer.normalize(TextNode.valueOf("ООО Ромашка"), strict))
                .isEqualTo("ООО Ромашка");
    }

    @Test
    void lowercasesWhenIgnoreCaseEnabled() {
        assertThat(normalizer.normalize(TextNode.valueOf("ООО РОМАШКА"), lenient))
                .isEqualTo("ооо ромашка");
    }

    @Test
    void keepsCaseWhenIgnoreCaseDisabled() {
        assertThat(normalizer.normalize(TextNode.valueOf("ООО РОМАШКА"), strict))
                .isEqualTo("ООО РОМАШКА");
    }

    @Test
    void trimsAndCollapsesSpacesWhenEnabled() {
        assertThat(normalizer.normalize(TextNode.valueOf("  ООО   Ромашка  "), lenient))
                .isEqualTo("ооо ромашка");
    }

    @Test
    void keepsSpacesWhenDisabled() {
        assertThat(normalizer.normalize(TextNode.valueOf("  ООО   Ромашка  "), strict))
                .isEqualTo("  ООО   Ромашка  ");
    }

    @Test
    void convertsNumberToText() {
        assertThat(normalizer.normalize(IntNode.valueOf(123), lenient))
                .isEqualTo("123");
    }

    @Test
    void convertsBooleanToText() {
        assertThat(normalizer.normalize(BooleanNode.TRUE, lenient))
                .isEqualTo("true");
    }

    @Test
    void convertsObjectToJsonString() {
        ObjectNode obj = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        obj.put("city", "Москва");

        assertThat(normalizer.normalize(obj, strict))
                .isEqualTo("{\"city\":\"Москва\"}");
    }

    @Test
    void convertsArrayToJsonString() {
        ArrayNode arr = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.arrayNode();
        arr.add("a");
        arr.add("b");

        assertThat(normalizer.normalize(arr, strict))
                .isEqualTo("[\"a\",\"b\"]");
    }
}