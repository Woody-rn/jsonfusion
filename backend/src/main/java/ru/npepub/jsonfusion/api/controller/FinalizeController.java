package ru.npepub.jsonfusion.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.npepub.jsonfusion.api.dto.request.FinalizeRequest;
import ru.npepub.jsonfusion.api.dto.response.FinalizeResponse;
import ru.npepub.jsonfusion.domain.exception.InvalidSessionStateException;
import ru.npepub.jsonfusion.domain.exception.SessionNotFoundException;
import ru.npepub.jsonfusion.domain.exception.UnmatchedResultNotFoundException;
import ru.npepub.jsonfusion.domain.model.json.JsonDocument;
import ru.npepub.jsonfusion.domain.model.session.MergeSession;
import ru.npepub.jsonfusion.domain.model.session.SessionStatus;
import ru.npepub.jsonfusion.domain.service.FinalizeOutcome;
import ru.npepub.jsonfusion.domain.service.FinalizeService;
import ru.npepub.jsonfusion.domain.service.SessionService;

/**
 * REST endpoints for finalization and downloading the result.
 */
@RestController
@RequestMapping("/api/sessions")
public class FinalizeController {

    private final SessionService sessionService;
    private final FinalizeService finalizeService;

    public FinalizeController(SessionService sessionService, FinalizeService finalizeService) {
        this.sessionService = sessionService;
        this.finalizeService = finalizeService;
    }

    @PostMapping("/{id}/finalize")
    public FinalizeResponse finalizeSession(@PathVariable String id,
                                            @Valid @RequestBody FinalizeRequest request) {
        MergeSession session = findSession(id);
        FinalizeOutcome outcome = finalizeService.finalizeSession(session, request.saveUnmatched());
        return FinalizeResponse.from(session, outcome);
    }

    @GetMapping("/{id}/result")
    public ResponseEntity<JsonDocument> downloadResult(@PathVariable String id) {
        MergeSession session = findSession(id);
        requireFinalized(session);
        return download(session.getFinalization().finalResult(), "merged-result.json");
    }

    @GetMapping("/{id}/result/unmatched")
    public ResponseEntity<JsonDocument> downloadUnmatched(@PathVariable String id) {
        MergeSession session = findSession(id);
        requireFinalized(session);
        JsonDocument unmatched = session.getFinalization().unmatchedResult();
        if (unmatched == null) {
            throw new UnmatchedResultNotFoundException(id);
        }
        return download(unmatched, "unmatched.json");
    }

    private void requireFinalized(MergeSession session) {
        if (session.getStatus() != SessionStatus.FINALIZED) {
            throw new InvalidSessionStateException(
                    "Result not available: session is in " + session.getStatus() + " state"
            );
        }
    }

    private ResponseEntity<JsonDocument> download(JsonDocument document, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(document);
    }

    private MergeSession findSession(String id) {
        return sessionService.findById(id)
                .orElseThrow(() -> new SessionNotFoundException(id));
    }
}