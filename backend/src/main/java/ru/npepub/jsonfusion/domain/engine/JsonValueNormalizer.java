package ru.npepub.jsonfusion.domain.engine;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import ru.npepub.jsonfusion.domain.model.config.ComparisonSettings;

import java.util.regex.Pattern;

/**
 * Normalizes anchor values before comparison: converts to string and applies
 * case and whitespace rules from {@link ComparisonSettings}.
 */
@Component
public class JsonValueNormalizer {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    /**
     * Normalizes the given node to a comparable string, or returns {@code null}
     * if the value is null or missing (such records cannot be matched).
     */
    public String normalize(JsonNode value, ComparisonSettings settings) {
        if (value == null || value.isNull()) {
            return null;
        }

        String result = toStringValue(value);

        if (settings.ignoreExtraSpaces()) {
            result = WHITESPACE.matcher(result.trim()).replaceAll(" ");
        }

        if (settings.ignoreCase()) {
            result = result.toLowerCase();
        }

        return result;
    }

    private String toStringValue(JsonNode value) {
        if (value.isTextual() || value.isNumber() || value.isBoolean()) {
            return value.asText();
        }
        return value.toString();
    }
}