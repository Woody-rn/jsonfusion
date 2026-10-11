package ru.npepub.jsonfusion.domain.service;

import org.springframework.stereotype.Service;
import ru.npepub.jsonfusion.domain.engine.FieldMerger;
import ru.npepub.jsonfusion.domain.exception.InvalidSessionStateException;
import ru.npepub.jsonfusion.domain.model.config.NewField;
import ru.npepub.jsonfusion.domain.model.json.JsonDocument;
import ru.npepub.jsonfusion.domain.model.json.JsonRecord;
import ru.npepub.jsonfusion.domain.model.result.Finalization;
import ru.npepub.jsonfusion.domain.model.result.MergeResult;
import ru.npepub.jsonfusion.domain.model.session.MergeSession;
import ru.npepub.jsonfusion.domain.model.session.SessionStatus;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedDecision;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedPair;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds the final result of a session from the merge result and user decisions.
 */
@Service
public class FinalizeService {

    private final FieldMerger fieldMerger;
    private final SessionService sessionService;

    public FinalizeService(FieldMerger fieldMerger, SessionService sessionService) {
        this.fieldMerger = fieldMerger;
        this.sessionService = sessionService;
    }

    public FinalizeOutcome finalizeSession(MergeSession session, boolean saveUnmatched) {
        if (session.getStatus() != SessionStatus.MERGED
                && session.getStatus() != SessionStatus.UNMATCHED_RESOLVED) {
            throw new InvalidSessionStateException(
                    "Cannot finalize: session is in " + session.getStatus() + " state"
            );
        }

        MergeResult result = session.getResult();
        UnmatchedDecision decision = session.getDecision();
        Set<String> ignoredIds = decision != null ? decision.ignoredIds() : Set.of();
        List<NewField> newFields = session.getConfig().newFields();

        List<JsonRecord> finalRecords = new ArrayList<>();

        for (JsonRecord r : result.merged()) {
            finalRecords.add(applyNewFields(r, newFields, finalRecords.size()));
        }
        int fromMatched = result.merged().size();
        int fromPairs = 0;

        Map<String, UnmatchedRecord> leftById = index(result.unmatchedLeft());
        Map<String, UnmatchedRecord> rightById = index(result.unmatchedRight());

        for (UnmatchedPair pair : pairs(decision)) {
            JsonRecord left = leftById.get(pair.leftId()).record();
            JsonRecord right = rightById.get(pair.rightId()).record();
            JsonRecord merged = fieldMerger.merge(left, right, session.getConfig().fieldRules(), 0);
            finalRecords.add(applyNewFields(merged, newFields, finalRecords.size()));
            fromPairs++;
        }

        List<JsonRecord> unmatchedRecords = new ArrayList<>();
        int unmatchedSaved = 0;
        int ignoredCount = 0;

        for (UnmatchedRecord r : result.unmatchedLeft()) {
            if (ignoredIds.contains(r.id())) {
                ignoredCount++;
            } else if (!isPaired(r.id(), decision)) {
                unmatchedRecords.add(r.record());
                unmatchedSaved++;
            }
        }
        for (UnmatchedRecord r : result.unmatchedRight()) {
            if (ignoredIds.contains(r.id())) {
                ignoredCount++;
            } else if (!isPaired(r.id(), decision)) {
                unmatchedRecords.add(r.record());
                unmatchedSaved++;
            }
        }

        JsonDocument finalResult = new JsonDocument(finalRecords);
        JsonDocument unmatchedResult = saveUnmatched && !unmatchedRecords.isEmpty()
                ? new JsonDocument(unmatchedRecords)
                : null;

        Finalization finalization = new Finalization(finalResult, unmatchedResult);
        session.setFinalization(finalization);
        sessionService.updateStatus(session, SessionStatus.FINALIZED);

        return new FinalizeOutcome(finalization, fromMatched, fromPairs, unmatchedSaved, ignoredCount);
    }

    private JsonRecord applyNewFields(JsonRecord record, List<NewField> newFields, int sourceIndex) {
        if (newFields.isEmpty()) {
            return new JsonRecord(record.fields(), sourceIndex);
        }
        Map<String, com.fasterxml.jackson.databind.JsonNode> fields = new LinkedHashMap<>(record.fields());
        for (NewField nf : newFields) {
            fields.put(nf.name(), nf.defaultValue());
        }
        return new JsonRecord(fields, sourceIndex);
    }

    private Map<String, UnmatchedRecord> index(List<UnmatchedRecord> records) {
        Map<String, UnmatchedRecord> map = new HashMap<>();
        for (UnmatchedRecord r : records) {
            map.put(r.id(), r);
        }
        return map;
    }

    private List<UnmatchedPair> pairs(UnmatchedDecision decision) {
        return decision != null ? decision.pairs() : List.of();
    }

    private boolean isPaired(String id, UnmatchedDecision decision) {
        if (decision == null) return false;
        for (UnmatchedPair pair : decision.pairs()) {
            if (pair.leftId().equals(id) || pair.rightId().equals(id)) {
                return true;
            }
        }
        return false;
    }
}