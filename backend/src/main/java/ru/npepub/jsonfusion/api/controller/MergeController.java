package ru.npepub.jsonfusion.api.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.npepub.jsonfusion.api.dto.response.MergeResponse;
import ru.npepub.jsonfusion.domain.exception.SessionNotFoundException;
import ru.npepub.jsonfusion.domain.model.session.MergeSession;
import ru.npepub.jsonfusion.domain.service.MergeService;
import ru.npepub.jsonfusion.domain.service.SessionService;

/**
 * REST endpoint for running a merge on a session.
 */
@RestController
@RequestMapping("/api/sessions")
public class MergeController {

    private final SessionService sessionService;
    private final MergeService mergeService;

    public MergeController(SessionService sessionService, MergeService mergeService) {
        this.sessionService = sessionService;
        this.mergeService = mergeService;
    }

    @PostMapping("/{id}/merge")
    public MergeResponse merge(@PathVariable String id) {
        MergeSession session = sessionService.findById(id)
                .orElseThrow(() -> new SessionNotFoundException(id));

        MergeSession merged = mergeService.merge(session);
        return MergeResponse.from(merged);
    }
}