package ru.npepub.jsonfusion.domain.engine;

import org.springframework.stereotype.Component;
import ru.npepub.jsonfusion.domain.model.config.ComparisonSettings;
import ru.npepub.jsonfusion.domain.model.json.JsonDocument;
import ru.npepub.jsonfusion.domain.model.json.JsonRecord;
import ru.npepub.jsonfusion.domain.model.config.MergeConfig;
import ru.npepub.jsonfusion.domain.model.result.MergeResult;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedRecord;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Core merge engine. Groups both documents by anchor, pairs unique anchors,
 * merges matched records, and collects unmatched records with their types.
 */
@Component
public class MergeEngine {

    private final AnchorMatcher matcher;
    private final RecordMerger recordMerger;

    public MergeEngine(AnchorMatcher matcher, RecordMerger recordMerger) {
        this.matcher = matcher;
        this.recordMerger = recordMerger;
    }

    public MergeResult merge(JsonDocument file1, JsonDocument file2, MergeConfig config) {
        ComparisonSettings settings = config.comparisonSettings();
        String anchorField = config.anchorField();

        AnchorMatcher.GroupResult left = matcher.group(file1, anchorField, settings);
        AnchorMatcher.GroupResult right = matcher.group(file2, anchorField, settings);

        List<JsonRecord> merged = recordMerger.mergeMatched(left, right, config);

        List<UnmatchedRecord> unmatchedLeft = collectLeft(left, right);
        List<UnmatchedRecord> unmatchedRight = collectRight(left, right);

        return new MergeResult(merged, unmatchedLeft, unmatchedRight);
    }

    private List<UnmatchedRecord> collectLeft(AnchorMatcher.GroupResult left,
                                              AnchorMatcher.GroupResult right) {
        List<UnmatchedRecord> result = new ArrayList<>();
        int counter = 1;

        for (Map.Entry<String, JsonRecord> entry : left.unique().entrySet()) {
            if (!right.unique().containsKey(entry.getKey())) {
                result.add(new UnmatchedRecord("L" + counter++, entry.getValue(),
                        UnmatchedType.ONLY_IN_LEFT, null, false));
            }
        }

        for (Map.Entry<String, List<JsonRecord>> entry : left.duplicates().entrySet()) {
            for (JsonRecord record : entry.getValue()) {
                result.add(new UnmatchedRecord("L" + counter++, record,
                        UnmatchedType.DUPLICATE, null, false));
            }
        }

        for (JsonRecord record : left.nullAnchors()) {
            result.add(new UnmatchedRecord("L" + counter++, record,
                    UnmatchedType.ONLY_IN_LEFT, null, false));
        }

        return result;
    }

    private List<UnmatchedRecord> collectRight(AnchorMatcher.GroupResult left,
                                               AnchorMatcher.GroupResult right) {
        List<UnmatchedRecord> result = new ArrayList<>();
        int counter = 1;

        for (Map.Entry<String, JsonRecord> entry : right.unique().entrySet()) {
            if (!left.unique().containsKey(entry.getKey())) {
                result.add(new UnmatchedRecord("R" + counter++, entry.getValue(),
                        UnmatchedType.ONLY_IN_RIGHT, null, false));
            }
        }

        for (Map.Entry<String, List<JsonRecord>> entry : right.duplicates().entrySet()) {
            for (JsonRecord record : entry.getValue()) {
                result.add(new UnmatchedRecord("R" + counter++, record,
                        UnmatchedType.DUPLICATE, null, false));
            }
        }

        for (JsonRecord record : right.nullAnchors()) {
            result.add(new UnmatchedRecord("R" + counter++, record,
                    UnmatchedType.ONLY_IN_RIGHT, null, false));
        }

        return result;
    }
}