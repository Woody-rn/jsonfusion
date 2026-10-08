package ru.npepub.jsonfusion.api.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.npepub.jsonfusion.api.dto.response.UploadFilesResponse;
import ru.npepub.jsonfusion.domain.exception.SessionNotFoundException;
import ru.npepub.jsonfusion.domain.model.session.MergeSession;
import ru.npepub.jsonfusion.domain.service.FileService;
import ru.npepub.jsonfusion.domain.service.SessionService;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * REST endpoint for uploading the two JSON files of a session.
 */
@RestController
@RequestMapping("/api/sessions")
public class FileController {

    private final SessionService sessionService;
    private final FileService fileService;

    public FileController(SessionService sessionService, FileService fileService) {
        this.sessionService = sessionService;
        this.fileService = fileService;
    }

    @PostMapping(value = "/{id}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFilesResponse upload(@PathVariable String id,
                                      @RequestParam("file1") MultipartFile file1,
                                      @RequestParam("file2") MultipartFile file2) {
        MergeSession session = sessionService.findById(id)
                .orElseThrow(() -> new SessionNotFoundException(id));

        try (InputStream in1 = file1.getInputStream();
             InputStream in2 = file2.getInputStream()) {

            MergeSession updated = fileService.upload(session, in1, in2);
            return UploadFilesResponse.from(updated);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}