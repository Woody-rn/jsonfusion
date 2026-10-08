package ru.npepub.jsonfusion.api.dto.response;

import ru.npepub.jsonfusion.domain.model.session.MergeSession;

import java.time.Instant;

/**
 * REST response for session endpoints.
 */
public record SessionResponse(
        String sessionId,
        String status,
        Instant createdAt,
        Instant lastAccessAt
) {

    public static SessionResponse from(MergeSession session) {
        return new SessionResponse(
                session.getId(),
                session.getStatus().name(),
                session.getCreatedAt(),
                session.getLastAccessAt()
        );
    }
}