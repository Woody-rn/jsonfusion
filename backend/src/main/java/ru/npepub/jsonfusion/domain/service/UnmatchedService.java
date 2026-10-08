package ru.npepub.jsonfusion.domain.service;

import org.springframework.stereotype.Service;
import ru.npepub.jsonfusion.domain.exception.InvalidSessionStateException;
import ru.npepub.jsonfusion.domain.exception.ValidationException;
import ru.npepub.jsonfusion.domain.model.result.MergeResult;
import ru.npepub.jsonfusion.domain.model.session.MergeSession;
import ru.npepub.jsonfusion.domain.model.session.SessionStatus;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedDecision;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedPair;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedRecord;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reads unmatched records of a session and stores user decisions about them.
 */
@Service
public class UnmatchedService {

    private final SessionService sessionService;

    public UnmatchedService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public MergeResult getResult(MergeSession session) {
        requireStatus(session, SessionStatus.MERGED, SessionStatus.UNMATCHED_RESOLVED);
        return session.getResult();
    }

    public UnmatchedDecision getDecision(MergeSession session) {
        return session.getDecision();
    }

    public UnmatchedDecision saveDecision(MergeSession session, UnmatchedDecision decision) {
        requireStatus(session, SessionStatus.MERGED, SessionStatus.UNMATCHED_RESOLVED);

        validate(session, decision);

        session.setDecision(decision);
        sessionService.updateStatus(session, SessionStatus.UNMATCHED_RESOLVED);
        return decision;
    }

    private void requireStatus(MergeSession session, SessionStatus... allowed) {
        for (SessionStatus s : allowed) {
            if (session.getStatus() == s) {
                return;
            }
        }
        throw new InvalidSessionStateException(
                "Operation not allowed in state " + session.getStatus()
        );
    }

    private void validate(MergeSession session, UnmatchedDecision decision) {
        MergeResult result = session.getResult();

        Set<String> leftIds = result.unmatchedLeft().stream()
                .map(UnmatchedRecord::id)
                .collect(Collectors.toSet());
        Set<String> rightIds = result.unmatchedRight().stream()
                .map(UnmatchedRecord::id)
                .collect(Collectors.toSet());

        Set<String> usedLeft = new HashSet<>();
        Set<String> usedRight = new HashSet<>();

        for (UnmatchedPair pair : decision.pairs()) {
            if (!leftIds.contains(pair.leftId())) {
                throw new ValidationException("Unknown leftId: " + pair.leftId());
            }
            if (!rightIds.contains(pair.rightId())) {
                throw new ValidationException("Unknown rightId: " + pair.rightId());
            }
            if (!usedLeft.add(pair.leftId())) {
                throw new ValidationException("leftId used in more than one pair: " + pair.leftId());
            }
            if (!usedRight.add(pair.rightId())) {
                throw new ValidationException("rightId used in more than one pair: " + pair.rightId());
            }
        }

        Set<String> allIds = Stream.concat(leftIds.stream(), rightIds.stream())
                .collect(Collectors.toSet());

        for (String id : decision.ignoredIds()) {
            if (!allIds.contains(id)) {
                throw new ValidationException("Unknown ignoredId: " + id);
            }
            if (usedLeft.contains(id) || usedRight.contains(id)) {
                throw new ValidationException("Id is both paired and ignored: " + id);
            }
        }
    }
}