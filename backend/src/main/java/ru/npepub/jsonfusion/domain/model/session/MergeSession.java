package ru.npepub.jsonfusion.domain.model.session;

import lombok.Getter;
import lombok.Setter;
import ru.npepub.jsonfusion.domain.model.json.JsonDocument;
import ru.npepub.jsonfusion.domain.model.config.MergeConfig;
import ru.npepub.jsonfusion.domain.model.result.MergeResult;

import java.time.Instant;

/**
 * State of a single merge operation. Lives in {@code SessionStore} between requests.
 */
@Getter
@Setter
public class MergeSession {

    private final String id;
    private SessionStatus status;
    private JsonDocument file1;
    private JsonDocument file2;
    private MergeConfig config;
    private MergeResult result;
    //private UnmatchedDecision decision;
    private final Instant createdAt;
    private Instant lastAccessAt;

    public MergeSession(String id, Instant createdAt) {
        this.id = id;
        this.status = SessionStatus.CREATED;
        this.createdAt = createdAt;
        this.lastAccessAt = createdAt;
    }

    public void touch() {
        this.lastAccessAt = Instant.now();
    }
}