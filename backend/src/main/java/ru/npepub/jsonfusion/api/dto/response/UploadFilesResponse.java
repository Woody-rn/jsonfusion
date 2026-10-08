package ru.npepub.jsonfusion.api.dto.response;

import ru.npepub.jsonfusion.domain.model.session.MergeSession;

/**
 * Response for POST /api/sessions/{id}/files.
 */
public record UploadFilesResponse(
        String sessionId,
        String status,
        FileAnalysis file1,
        FileAnalysis file2
) {

    public static UploadFilesResponse from(MergeSession session) {
        return new UploadFilesResponse(
                session.getId(),
                session.getStatus().name(),
                FileAnalysis.from(session.getFile1()),
                FileAnalysis.from(session.getFile2())
        );
    }
}