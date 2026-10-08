package ru.npepub.jsonfusion.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.npepub.jsonfusion.domain.exception.InvalidSessionStateException;
import ru.npepub.jsonfusion.domain.exception.ValidationException;
import ru.npepub.jsonfusion.domain.model.json.JsonRecord;
import ru.npepub.jsonfusion.domain.model.result.MergeResult;
import ru.npepub.jsonfusion.domain.model.session.MergeSession;
import ru.npepub.jsonfusion.domain.model.session.SessionStatus;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedDecision;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedPair;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedRecord;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedType;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UnmatchedServiceTest {

    private SessionService sessionService;
    private UnmatchedService service;

    @BeforeEach
    void setUp() {
        sessionService = mock(SessionService.class);
        service = new UnmatchedService(sessionService);
    }

    @Test
    void acceptsValidDecision() {
        MergeSession session = sessionWithUnmatched();
        UnmatchedDecision decision = new UnmatchedDecision(
                List.of(new UnmatchedPair("L1", "R1", "1")),
                Set.of("L2")
        );

        service.saveDecision(session, decision);

        assertThat(session.getDecision()).isEqualTo(decision);
        assertThat(session.getDecision()).isEqualTo(decision);
        verify(sessionService).updateStatus(eq(session), eq(SessionStatus.UNMATCHED_RESOLVED));
        verify(sessionService).updateStatus(eq(session), eq(SessionStatus.UNMATCHED_RESOLVED));
    }

    @Test
    void rejectsUnknownLeftId() {
        MergeSession session = sessionWithUnmatched();
        UnmatchedDecision decision = new UnmatchedDecision(
                List.of(new UnmatchedPair("L99", "R1", "1")),
                Set.of()
        );

        assertThatThrownBy(() -> service.saveDecision(session, decision))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Unknown leftId");
    }

    @Test
    void rejectsUnknownRightId() {
        MergeSession session = sessionWithUnmatched();
        UnmatchedDecision decision = new UnmatchedDecision(
                List.of(new UnmatchedPair("L1", "R99", "1")),
                Set.of()
        );

        assertThatThrownBy(() -> service.saveDecision(session, decision))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Unknown rightId");
    }

    @Test
    void rejectsLeftIdUsedInTwoPairs() {
        MergeSession session = sessionWithUnmatched();
        UnmatchedDecision decision = new UnmatchedDecision(
                List.of(
                        new UnmatchedPair("L1", "R1", "1"),
                        new UnmatchedPair("L1", "R2", "2")
                ),
                Set.of()
        );

        assertThatThrownBy(() -> service.saveDecision(session, decision))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("leftId used in more than one pair");
    }

    @Test
    void rejectsIdBothPairedAndIgnored() {
        MergeSession session = sessionWithUnmatched();
        UnmatchedDecision decision = new UnmatchedDecision(
                List.of(new UnmatchedPair("L1", "R1", "1")),
                Set.of("L1")
        );

        assertThatThrownBy(() -> service.saveDecision(session, decision))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("both paired and ignored");
    }

    @Test
    void rejectsUnknownIgnoredId() {
        MergeSession session = sessionWithUnmatched();
        UnmatchedDecision decision = new UnmatchedDecision(
                List.of(),
                Set.of("L99")
        );

        assertThatThrownBy(() -> service.saveDecision(session, decision))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Unknown ignoredId");
    }

    @Test
    void rejectsWhenSessionInWrongState() {
        MergeSession session = sessionWithUnmatched();
        session.setStatus(SessionStatus.CONFIGURED);

        UnmatchedDecision decision = new UnmatchedDecision(List.of(), Set.of());

        assertThatThrownBy(() -> service.saveDecision(session, decision))
                .isInstanceOf(InvalidSessionStateException.class);
    }

    private MergeSession sessionWithUnmatched() {
        MergeSession session = new MergeSession("test-id", Instant.now());
        session.setStatus(SessionStatus.MERGED);

        UnmatchedRecord l1 = new UnmatchedRecord("L1", record("A"), UnmatchedType.ONLY_IN_LEFT, null, false);
        UnmatchedRecord l2 = new UnmatchedRecord("L2", record("B"), UnmatchedType.DUPLICATE, null, false);
        UnmatchedRecord r1 = new UnmatchedRecord("R1", record("C"), UnmatchedType.ONLY_IN_RIGHT, null, false);
        UnmatchedRecord r2 = new UnmatchedRecord("R2", record("D"), UnmatchedType.ONLY_IN_RIGHT, null, false);

        MergeResult result = new MergeResult(List.of(), List.of(l1, l2), List.of(r1, r2));
        session.setResult(result);
        return session;
    }

    private JsonRecord record(String name) {
        return new JsonRecord(Map.of("name", com.fasterxml.jackson.databind.node.TextNode.valueOf(name)), 0);
    }
}