package ru.npepub.jsonfusion.domain.service;

import org.springframework.stereotype.Service;
import ru.npepub.jsonfusion.domain.exception.InvalidSessionStateException;
import ru.npepub.jsonfusion.domain.model.json.JsonDocument;
import ru.npepub.jsonfusion.domain.model.session.MergeSession;
import ru.npepub.jsonfusion.domain.model.session.SessionStatus;
import ru.npepub.jsonfusion.domain.port.JsonParser;

import java.io.InputStream;

/**
 * Handles file upload and parsing for a session.
 */
@Service
public class FileService {

    private final JsonParser jsonParser;
    private final SessionService sessionService;

    public FileService(JsonParser jsonParser, SessionService sessionService) {
        this.jsonParser = jsonParser;
        this.sessionService = sessionService;
    }

    /**
     * Parses the two uploaded files and attaches them to the session.
     * The session must be in {@link SessionStatus#CREATED} state.
     *
     * @param session session to attach files to
     * @param file1Stream stream of the first file
     * @param file2Stream stream of the second file
     * @return the same session with files attached and status FILES_UPLOADED
     * @throws InvalidSessionStateException if the session is not in CREATED state
     */
    public MergeSession upload(MergeSession session,
                               InputStream file1Stream,
                               InputStream file2Stream) {
        if (session.getStatus() != SessionStatus.CREATED) {
            throw new InvalidSessionStateException(
                    "Cannot upload files: session is in " + session.getStatus() + " state"
            );
        }

        JsonDocument file1 = jsonParser.parse(file1Stream);
        JsonDocument file2 = jsonParser.parse(file2Stream);

        session.setFile1(file1);
        session.setFile2(file2);
        sessionService.updateStatus(session, SessionStatus.FILES_UPLOADED);

        return session;
    }
}