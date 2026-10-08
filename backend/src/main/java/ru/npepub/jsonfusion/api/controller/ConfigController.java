package ru.npepub.jsonfusion.api.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.npepub.jsonfusion.api.dto.request.UpdateConfigRequest;
import ru.npepub.jsonfusion.api.dto.response.FieldsResponse;
import ru.npepub.jsonfusion.domain.exception.SessionNotFoundException;
import ru.npepub.jsonfusion.domain.model.MergeConfig;
import ru.npepub.jsonfusion.domain.model.MergeSession;
import ru.npepub.jsonfusion.domain.service.ConfigService;
import ru.npepub.jsonfusion.domain.service.SessionService;

/**
 * REST endpoints for reading and updating the merge config of a session.
 */
@RestController
@RequestMapping("/api/sessions")
public class ConfigController {

    private final SessionService sessionService;
    private final ConfigService configService;

    public ConfigController(SessionService sessionService, ConfigService configService) {
        this.sessionService = sessionService;
        this.configService = configService;
    }

    @GetMapping("/{id}/fields")
    public FieldsResponse getFields(@PathVariable String id) {
        MergeSession session = findSession(id);
        MergeConfig config = session.getConfig() != null
                ? session.getConfig()
                : configService.buildDefault(session);
        return FieldsResponse.from(config, session.getFile1(), session.getFile2());
    }

    @PutMapping("/{id}/config")
    public FieldsResponse updateConfig(@PathVariable String id,
                                       @Valid @RequestBody UpdateConfigRequest request) {
        MergeSession session = findSession(id);
        MergeConfig config = configService.update(session, request);
        return FieldsResponse.from(config, session.getFile1(), session.getFile2());
    }

    private MergeSession findSession(String id) {
        return sessionService.findById(id)
                .orElseThrow(() -> new SessionNotFoundException(id));
    }
}