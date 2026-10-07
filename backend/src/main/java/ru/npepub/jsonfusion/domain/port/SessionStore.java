package ru.npepub.jsonfusion.domain.port;

import ru.npepub.jsonfusion.domain.model.MergeSession;

import java.util.Optional;

/**
 * Port for storing merge sessions. Implementation decides the storage mechanism
 * (e.g. in-memory Caffeine, Redis).
 */
public interface SessionStore {

    /**
     * Creates a new session with a generated id and current timestamp.
     *
     * @return the newly created session
     */
    MergeSession create();

    /**
     * Returns a session by id, if present.
     *
     * @param id session id
     * @return session or empty
     */
    Optional<MergeSession> findById(String id);

    /**
     * Saves the session. Creates it if absent, updates otherwise.
     *
     * @param session session to save
     */
    void save(MergeSession session);

    /**
     * Deletes a session by id. No-op if absent.
     *
     * @param id session id
     */
    void delete(String id);
}