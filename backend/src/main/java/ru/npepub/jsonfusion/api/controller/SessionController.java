package ru.npepub.jsonfusion.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.npepub.jsonfusion.api.dto.response.SessionResponse;
import ru.npepub.jsonfusion.domain.exception.SessionNotFoundException;
import ru.npepub.jsonfusion.domain.model.session.MergeSession;
import ru.npepub.jsonfusion.domain.service.SessionService;

/**
 * REST endpoints for session lifecycle.
 */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<SessionResponse> create() {
        MergeSession session = sessionService.create();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SessionResponse.from(session));
    }

    @GetMapping("/{id}")
    public SessionResponse get(@PathVariable String id) {
        MergeSession session = sessionService.findById(id)
                .orElseThrow(() -> new SessionNotFoundException(id));
        return SessionResponse.from(session);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean deleted = sessionService.delete(id);
        if (!deleted) {
            throw new SessionNotFoundException(id);
        }
        return ResponseEntity.noContent().build();
    }
}