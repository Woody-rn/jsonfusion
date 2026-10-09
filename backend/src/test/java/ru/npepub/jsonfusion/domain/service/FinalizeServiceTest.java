package ru.npepub.jsonfusion.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.npepub.jsonfusion.domain.engine.FieldMerger;
import ru.npepub.jsonfusion.domain.exception.InvalidSessionStateException;
import ru.npepub.jsonfusion.domain.model.config.ComparisonSettings;
import ru.npepub.jsonfusion.domain.model.config.FieldRole;
import ru.npepub.jsonfusion.domain.model.config.FieldRule;
import ru.npepub.jsonfusion.domain.model.config.MergeConfig;
import ru.npepub.jsonfusion.domain.model.config.PriorityMode;
import ru.npepub.jsonfusion.domain.model.json.JsonRecord;
import ru.npepub.jsonfusion.domain.model.result.MergeResult;
import ru.npepub.jsonfusion.domain.model.session.MergeSession;
import ru.npepub.jsonfusion.domain.model.session.SessionStatus;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedDecision;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedPair;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedRecord;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class FinalizeServiceTest {

    private FinalizeService service;

    @BeforeEach
    void setUp() {
        service = new FinalizeService(new FieldMerger(), mock(SessionService.class));
    }

    @Test
    void includesOnlyMatchedWhenNoDecision() {
        MergeSession session = sessionWith(
                List.of(record("name", "A")),
                List.of(record("name", "B")),
                List.of(record("name", "C")),
                null
        );

        FinalizeOutcome outcome = service.finalizeSession(session, true);

        assertThat(outcome.finalization().finalResult().size()).isEqualTo(1);
        assertThat(outcome.fromMatched()).isEqualTo(1);
        assertThat(outcome.fromPairs()).isZero();
        assertThat(outcome.unmatchedSaved()).isEqualTo(2);
        assertThat(outcome.ignoredCount()).isZero();
    }

    @Test
    void manualPairIsMergedIntoFinalResult() {
        MergeSession session = sessionWith(
                List.of(),
                List.of(record("name", "Left")),
                List.of(record("name", "Right")),
                new UnmatchedDecision(List.of(new UnmatchedPair("L1", "R1", "1")), Set.of())
        );

        FinalizeOutcome outcome = service.finalizeSession(session, true);

        assertThat(outcome.finalization().finalResult().size()).isEqualTo(1);
        assertThat(outcome.fromPairs()).isEqualTo(1);
        assertThat(outcome.unmatchedSaved()).isZero();
    }

    @Test
    void ignoredRecordsAreExcludedEverywhere() {
        MergeSession session = sessionWith(
                List.of(),
                List.of(record("name", "Left"), record("name", "Left2")),
                List.of(record("name", "Right")),
                new UnmatchedDecision(List.of(), Set.of("L1", "R1"))
        );

        FinalizeOutcome outcome = service.finalizeSession(session, true);

        assertThat(outcome.ignoredCount()).isEqualTo(2);
        assertThat(outcome.unmatchedSaved()).isEqualTo(1);
    }

    @Test
    void unmatchedResultIsNullWhenSaveUnmatchedFalse() {
        MergeSession session = sessionWith(
                List.of(),
                List.of(record("name", "Left")),
                List.of(),
                null
        );

        FinalizeOutcome outcome = service.finalizeSession(session, false);

        assertThat(outcome.finalization().unmatchedResult()).isNull();
        assertThat(outcome.unmatchedSaved()).isEqualTo(1);
    }

    @Test
    void unmatchedResultIsBuiltWhenSaveUnmatchedTrue() {
        MergeSession session = sessionWith(
                List.of(),
                List.of(record("name", "Left")),
                List.of(record("name", "Right")),
                null
        );

        FinalizeOutcome outcome = service.finalizeSession(session, true);

        assertThat(outcome.finalization().unmatchedResult()).isNotNull();
        assertThat(outcome.finalization().unmatchedResult().size()).isEqualTo(2);
    }

    @Test
    void rejectsFinalizeInWrongState() {
        MergeSession session = sessionWith(
                List.of(),
                List.of(),
                List.of(),
                null
        );
        session.setStatus(SessionStatus.CONFIGURED);

        assertThatThrownBy(() -> service.finalizeSession(session, true))
                .isInstanceOf(InvalidSessionStateException.class);
    }

    private MergeSession sessionWith(List<JsonRecord> merged,
                                     List<JsonRecord> left,
                                     List<JsonRecord> right,
                                     UnmatchedDecision decision) {
        MergeSession session = new MergeSession("test-id", Instant.now());
        session.setStatus(SessionStatus.MERGED);

        MergeResult result = new MergeResult(
                merged,
                toUnmatched(left, "L", UnmatchedType.ONLY_IN_LEFT),
                toUnmatched(right, "R", UnmatchedType.ONLY_IN_RIGHT)
        );
        session.setResult(result);
        session.setDecision(decision);
        session.setConfig(config());
        return session;
    }

    private List<UnmatchedRecord> toUnmatched(List<JsonRecord> records, String prefix, UnmatchedType type) {
        List<UnmatchedRecord> list = new ArrayList<>();
        int i = 1;
        for (JsonRecord r : records) {
            list.add(new UnmatchedRecord(prefix + i++, r, type, null, false));
        }
        return list;
    }

    private MergeConfig config() {
        return new MergeConfig(
                List.of(
                        rule("name", FieldRole.ANCHOR),
                        rule("status", FieldRole.PRIORITY_F1)
                ),
                "name",
                new ComparisonSettings(true, true),
                List.of()
        );
    }

    private FieldRule rule(String name, FieldRole role) {
        PriorityMode mode = (role == FieldRole.PRIORITY_F1 || role == FieldRole.PRIORITY_F2)
                ? PriorityMode.ALWAYS
                : null;
        return new FieldRule(name, role, mode, null, false);
    }

    private JsonRecord record(String... keyValues) {
        Map<String, JsonNode> fields = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            fields.put(keyValues[i], TextNode.valueOf(keyValues[i + 1]));
        }
        return new JsonRecord(fields, 0);
    }
}