package ru.npepub.jsonfusion.infrastructure.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.npepub.jsonfusion.domain.exception.InvalidJsonException;
import ru.npepub.jsonfusion.domain.model.JsonDocument;
import ru.npepub.jsonfusion.domain.model.JsonRecord;
import ru.npepub.jsonfusion.domain.port.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link JsonParser} implementation backed by Jackson.
 * Validates that the input is a JSON array of objects.
 */
@Component
public class JacksonJsonParser implements JsonParser {

    private final ObjectMapper objectMapper;

    public JacksonJsonParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public JsonDocument parse(InputStream input) {
        JsonNode root;
        try {
            root = objectMapper.readTree(input);
        } catch (IOException e) {
            throw new InvalidJsonException("Failed to parse JSON: " + e.getMessage(), e);
        }

        if (!root.isArray()) {
            throw new InvalidJsonException("Root element must be a JSON array");
        }

        List<JsonRecord> records = new ArrayList<>();
        int index = 0;
        for (JsonNode node : root) {
            if (!node.isObject()) {
                throw new InvalidJsonException("Record at index " + index + " must be a JSON object");
            }
            records.add(toRecord(node, index));
            index++;
        }

        return new JsonDocument(records);
    }

    private JsonRecord toRecord(JsonNode node, int index) {
        Map<String, JsonNode> fields = new LinkedHashMap<>();
        node.fields().forEachRemaining(entry -> fields.put(entry.getKey(), entry.getValue()));
        return new JsonRecord(fields, index);
    }
}