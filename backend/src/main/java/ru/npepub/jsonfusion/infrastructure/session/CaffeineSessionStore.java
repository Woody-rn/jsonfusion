package ru.npepub.jsonfusion.infrastructure.session;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;
import ru.npepub.jsonfusion.domain.model.session.MergeSession;
import ru.npepub.jsonfusion.domain.port.SessionStore;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory {@link SessionStore} backed by Caffeine.
 * Sessions expire after a period of inactivity.
 */
@Component
public class CaffeineSessionStore implements SessionStore {

    private static final Duration IDLE_TIMEOUT = Duration.ofMinutes(15);
    private static final int MAX_SESSIONS = 100;

    private final Cache<String, MergeSession> cache;

    public CaffeineSessionStore() {
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(IDLE_TIMEOUT)
                .maximumSize(MAX_SESSIONS)
                .build();
    }

    @Override
    public MergeSession create() {
        String id = UUID.randomUUID().toString();
        MergeSession session = new MergeSession(id, Instant.now());
        cache.put(id, session);
        return session;
    }

    @Override
    public Optional<MergeSession> findById(String id) {
        return Optional.ofNullable(cache.getIfPresent(id));
    }

    @Override
    public void save(MergeSession session) {
        cache.put(session.getId(), session);
    }

    @Override
    public void delete(String id) {
        cache.invalidate(id);
    }
}