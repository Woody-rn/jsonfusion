package ru.npepub.jsonfusion.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.npepub.jsonfusion.api.dto.request.FinalizeRequest;
import ru.npepub.jsonfusion.api.dto.response.FinalizeResponse;
import ru.npepub.jsonfusion.domain.exception.InvalidSessionStateException;
import ru.npepub.jsonfusion.domain.exception.UnmatchedResultNotFoundException;
import ru.npepub.jsonfusion.domain.exception.SessionNotFoundException;
import ru.npepub.jsonfusion.domain.model.json.JsonDocument;
import ru.npepub.jsonfusion.domain.model.json.JsonRecord;
import ru.npepub.jsonfusion.domain.model.session.MergeSession;
import ru.npepub.jsonfusion.domain.model.session.SessionStatus;
import ru.npepub.jsonfusion.domain.service.FinalizeOutcome;
import ru.npepub.jsonfusion.domain.service.FinalizeService;
import ru.npepub.jsonfusion.domain.service.SessionService;

import java.util.Map;

/**
 * REST endpoints for finalization and downloading the result.
 */
@RestController
@RequestMapping("/api/sessions")
public class FinalizeController {

    private final SessionService sessionService;
    private final FinalizeService finalizeService;
    private final ObjectMapper objectMapper;

    public FinalizeController(SessionService sessionService,
                              FinalizeService finalizeService,
                              ObjectMapper objectMapper) {
        this.sessionService = sessionService;
        this.finalizeService = finalizeService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/{id}/finalize")
    public FinalizeResponse finalizeSession(@PathVariable String id,
                                            @Valid @RequestBody FinalizeRequest request) {
        MergeSession session = findSession(id);
        FinalizeOutcome outcome = finalizeService.finalizeSession(session, request.saveUnmatched());
        return FinalizeResponse.from(session, outcome);
    }

    @GetMapping("/{id}/result")
    public ResponseEntity<JsonNode> downloadResult(@PathVariable String id) {
        MergeSession session = findSession(id);
        requireFinalized(session);
        JsonNode body = toJsonArray(session.getFinalization().finalResult());
        return download(body, "merged-result.json");
    }

    @GetMapping("/{id}/result/unmatched")
    public ResponseEntity<JsonNode> downloadUnmatched(@PathVariable String id) {
        MergeSession session = findSession(id);
        requireFinalized(session);
        JsonDocument unmatched = session.getFinalization().unmatchedResult();
        if (unmatched == null) {
            throw new UnmatchedResultNotFoundException(id);
        }
        JsonNode body = toJsonArray(unmatched);
        return download(body, "unmatched.json");
    }

    private void requireFinalized(MergeSession session) {
        if (session.getStatus() != SessionStatus.FINALIZED) {
            throw new InvalidSessionStateException(
                    "Result not available: session is in " + session.getStatus() + " state"
            );
        }
    }

    private JsonNode toJsonArray(JsonDocument document) {
        ArrayNode array = objectMapper.createArrayNode();
        for (JsonRecord record : document.records()) {
            ObjectNode obj = objectMapper.createObjectNode();
            for (Map.Entry<String, JsonNode> entry : record.fields().entrySet()) {
                obj.set(entry.getKey(), entry.getValue());
            }
            array.add(obj);
        }
        return array;
    }

    private ResponseEntity<JsonNode> download(JsonNode body, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    private MergeSession findSession(String id) {
        return sessionService.findById(id)
                .orElseThrow(() -> new SessionNotFoundException(id));
    }
}