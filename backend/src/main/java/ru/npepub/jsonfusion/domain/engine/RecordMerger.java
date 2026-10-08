package ru.npepub.jsonfusion.domain.engine;

import org.springframework.stereotype.Component;
import ru.npepub.jsonfusion.domain.model.json.JsonRecord;
import ru.npepub.jsonfusion.domain.model.config.MergeConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Merges matched records from two grouped documents using field rules.
 */
@Component
public class RecordMerger {

    private final FieldMerger fieldMerger;

    public RecordMerger(FieldMerger fieldMerger) {
        this.fieldMerger = fieldMerger;
    }

    public List<JsonRecord> mergeMatched(AnchorMatcher.GroupResult left,
                                         AnchorMatcher.GroupResult right,
                                         MergeConfig config) {
        Map<String, JsonRecord> leftUnique = left.unique();
        Map<String, JsonRecord> rightUnique = right.unique();

        List<JsonRecord> result = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, JsonRecord> entry : leftUnique.entrySet()) {
            JsonRecord rightRecord = rightUnique.get(entry.getKey());
            if (rightRecord == null) {
                continue;
            }
            JsonRecord merged = fieldMerger.merge(entry.getValue(), rightRecord, config.fieldRules(), index++);
            result.add(merged);
        }
        return result;
    }
}