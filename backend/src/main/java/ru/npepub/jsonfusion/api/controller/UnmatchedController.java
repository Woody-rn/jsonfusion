package ru.npepub.jsonfusion.api.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.npepub.jsonfusion.api.dto.request.UpdateUnmatchedRequest;
import ru.npepub.jsonfusion.api.dto.response.UnmatchedRecordDto;
import ru.npepub.jsonfusion.api.dto.response.UnmatchedResponse;
import ru.npepub.jsonfusion.domain.exception.SessionNotFoundException;
import ru.npepub.jsonfusion.domain.model.result.MergeResult;
import ru.npepub.jsonfusion.domain.model.session.MergeSession;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedDecision;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedPair;
import ru.npepub.jsonfusion.domain.model.unmatched.UnmatchedRecord;
import ru.npepub.jsonfusion.domain.service.SessionService;
import ru.npepub.jsonfusion.domain.service.UnmatchedService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * REST endpoints for unmatched records and their user decisions.
 */
@RestController
@RequestMapping("/api/sessions")
public class UnmatchedController {

    private final SessionService sessionService;
    private final UnmatchedService unmatchedService;

    public UnmatchedController(SessionService sessionService, UnmatchedService unmatchedService) {
        this.sessionService = sessionService;
        this.unmatchedService = unmatchedService;
    }

    @GetMapping("/{id}/unmatched")
    public UnmatchedResponse get(@PathVariable String id) {
        MergeSession session = findSession(id);
        MergeResult result = unmatchedService.getResult(session);
        return toResponse(result, unmatchedService.getDecision(session));
    }

    @PutMapping("/{id}/unmatched/decisions")
    public UnmatchedResponse update(@PathVariable String id,
                                    @Valid @RequestBody UpdateUnmatchedRequest request) {
        MergeSession session = findSession(id);
        UnmatchedDecision saved = unmatchedService.saveDecision(session, request.toDomain());
        return toResponse(session.getResult(), saved);
    }

    private UnmatchedResponse toResponse(MergeResult result, UnmatchedDecision decision) {
        Map<String, UnmatchedPair> leftPairs = pairsByLeft(decision);
        Map<String, UnmatchedPair> rightPairs = pairsByRight(decision);
        Set<String> ignored = decision != null ? decision.ignoredIds() : Set.of();

        List<UnmatchedRecordDto> left = result.unmatchedLeft().stream()
                .map(r -> toDto(r, leftPairs.get(r.id()), ignored))
                .toList();
        List<UnmatchedRecordDto> right = result.unmatchedRight().stream()
                .map(r -> toDto(r, rightPairs.get(r.id()), ignored))
                .toList();

        return new UnmatchedResponse(left, right);
    }

    private UnmatchedRecordDto toDto(UnmatchedRecord record, UnmatchedPair pair, Set<String> ignored) {
        String label = pair != null ? pair.label() : null;
        boolean isIgnored = ignored.contains(record.id());
        return UnmatchedRecordDto.from(record, label, isIgnored);
    }

    private Map<String, UnmatchedPair> pairsByLeft(UnmatchedDecision decision) {
        if (decision == null) return Map.of();
        return decision.pairs().stream()
                .collect(Collectors.toMap(UnmatchedPair::leftId, Function.identity()));
    }

    private Map<String, UnmatchedPair> pairsByRight(UnmatchedDecision decision) {
        if (decision == null) return Map.of();
        return decision.pairs().stream()
                .collect(Collectors.toMap(UnmatchedPair::rightId, Function.identity()));
    }

    private MergeSession findSession(String id) {
        return sessionService.findById(id)
                .orElseThrow(() -> new SessionNotFoundException(id));
    }
}