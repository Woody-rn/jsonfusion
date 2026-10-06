package ru.npepub.jsonfusion.domain.service;

import org.springframework.stereotype.Service;
import ru.npepub.jsonfusion.domain.model.MergeSession;
import ru.npepub.jsonfusion.domain.model.SessionStatus;
import ru.npepub.jsonfusion.port.SessionStore;

import java.util.Optional;

/**
 * Manages the lifecycle of merge sessions.
 */
@Service
public class SessionService {

    private final SessionStore sessionStore;

    public SessionService(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    public MergeSession create() {
        return sessionStore.create();
    }

    /**
     * Finds a session and updates its last access timestamp.
     *
     * @param id session id
     * @return session or empty
     */
    public Optional<MergeSession> findById(String id) {
        Optional<MergeSession> session = sessionStore.findById(id);
        session.ifPresent(s -> {
            s.touch();
            sessionStore.save(s);
        });
        return session;
    }

    /**
     * Deletes a session by id.
     *
     * @param id session id
     * @return true if the session existed
     */
    public boolean delete(String id) {
        Optional<MergeSession> session = sessionStore.findById(id);
        if (session.isEmpty()) {
            return false;
        }
        sessionStore.delete(id);
        return true;
    }

    /**
     * Updates the status of a session.
     *
     * @param session session to update
     * @param status new status
     */
    public void updateStatus(MergeSession session, SessionStatus status) {
        session.setStatus(status);
        sessionStore.save(session);
    }
}