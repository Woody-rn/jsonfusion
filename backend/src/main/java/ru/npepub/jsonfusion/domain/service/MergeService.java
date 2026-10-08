package ru.npepub.jsonfusion.domain.service;

import org.springframework.stereotype.Service;
import ru.npepub.jsonfusion.domain.engine.MergeEngine;
import ru.npepub.jsonfusion.domain.exception.InvalidSessionStateException;
import ru.npepub.jsonfusion.domain.model.result.MergeResult;
import ru.npepub.jsonfusion.domain.model.session.MergeSession;
import ru.npepub.jsonfusion.domain.model.session.SessionStatus;

/**
 * Runs the merge for a session and stores the result.
 */
@Service
public class MergeService {

    private final MergeEngine mergeEngine;
    private final SessionService sessionService;

    public MergeService(MergeEngine mergeEngine, SessionService sessionService) {
        this.mergeEngine = mergeEngine;
        this.sessionService = sessionService;
    }

    /**
     * Merges the two files of the session. The session must be in CONFIGURED state.
     *
     * @param session session to merge
     * @return the same session with the merge result attached and status MERGED
     */
    public MergeSession merge(MergeSession session) {
        if (session.getStatus() != SessionStatus.CONFIGURED) {
            throw new InvalidSessionStateException(
                    "Cannot merge: session is in " + session.getStatus() + " state"
            );
        }

        MergeResult result = mergeEngine.merge(session.getFile1(), session.getFile2(), session.getConfig());
        session.setResult(result);
        sessionService.updateStatus(session, SessionStatus.MERGED);

        return session;
    }
}