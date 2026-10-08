package ru.npepub.jsonfusion.domain.engine;

import org.springframework.stereotype.Component;
import ru.npepub.jsonfusion.domain.model.ComparisonSettings;
import ru.npepub.jsonfusion.domain.model.JsonDocument;
import ru.npepub.jsonfusion.domain.model.JsonRecord;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Groups records by normalized anchor value and pairs those that appear exactly once
 * on each side. Records with null/missing anchor or with duplicated anchor values
 * are returned as unmatched candidates.
 */
@Component
public class AnchorMatcher {

    private final JsonValueNormalizer normalizer;

    public AnchorMatcher(JsonValueNormalizer normalizer) {
        this.normalizer = normalizer;
    }

    /**
     * Result of grouping a single document by anchor value.
     * {@code unique} holds anchors with exactly one record;
     * {@code duplicates} holds anchors with two or more records;
     * {@code nullAnchors} holds records whose anchor value is null or missing.
     */
    public record GroupResult(
            Map<String, JsonRecord> unique,
            Map<String, List<JsonRecord>> duplicates,
            List<JsonRecord> nullAnchors
    ) {
        public GroupResult {
            unique = Map.copyOf(unique);
            duplicates = Map.copyOf(duplicates);
            nullAnchors = List.copyOf(nullAnchors);
        }
    }

    public GroupResult group(JsonDocument document,
                             String anchorField,
                             ComparisonSettings settings) {
        Map<String, List<JsonRecord>> byKey = new LinkedHashMap<>();
        List<JsonRecord> nullAnchors = new ArrayList<>();

        for (JsonRecord record : document.records()) {
            String key = record.getField(anchorField)
                    .map(n -> normalizer.normalize(n, settings))
                    .orElse(null);

            if (key == null) {
                nullAnchors.add(record);
                continue;
            }

            byKey.computeIfAbsent(key, k -> new ArrayList<>()).add(record);
        }

        Map<String, JsonRecord> unique = new LinkedHashMap<>();
        Map<String, List<JsonRecord>> duplicates = new LinkedHashMap<>();

        byKey.forEach((key, records) -> {
            if (records.size() == 1) {
                unique.put(key, records.get(0));
            } else {
                duplicates.put(key, records);
            }
        });

        return new GroupResult(unique, duplicates, nullAnchors);
    }
}