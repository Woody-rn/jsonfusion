package ru.npepub.jsonfusion.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.npepub.jsonfusion.api.dto.AnchorSettingsDto;
import ru.npepub.jsonfusion.api.dto.NewFieldDto;
import ru.npepub.jsonfusion.api.dto.request.FieldRuleDto;
import ru.npepub.jsonfusion.api.dto.request.UpdateConfigRequest;
import ru.npepub.jsonfusion.domain.exception.InvalidSessionStateException;
import ru.npepub.jsonfusion.domain.exception.ValidationException;
import ru.npepub.jsonfusion.domain.model.config.FieldRole;
import ru.npepub.jsonfusion.domain.model.session.MergeSession;
import ru.npepub.jsonfusion.domain.model.config.PriorityMode;
import ru.npepub.jsonfusion.domain.model.session.SessionStatus;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ConfigServiceTest {

    private SessionService sessionService;
    private ConfigService configService;

    @BeforeEach
    void setUp() {
        sessionService = mock(SessionService.class);
        configService = new ConfigService(mock(FieldsAnalyzerService.class), sessionService);
    }

    @Test
    void acceptsValidConfig() {
        MergeSession session = session(SessionStatus.FILES_UPLOADED);
        UpdateConfigRequest request = request(
                List.of(
                        field("name", FieldRole.ANCHOR),
                        field("status", FieldRole.PRIORITY_F1)
                ),
                List.of()
        );

        configService.update(session, request);

        assertThat(session.getConfig()).isNotNull();
        assertThat(session.getConfig().anchorField()).isEqualTo("name");
        verify(sessionService).updateStatus(eq(session), eq(SessionStatus.CONFIGURED));
    }

    @Test
    void rejectsConfigWithoutAnchor() {
        MergeSession session = session(SessionStatus.FILES_UPLOADED);
        UpdateConfigRequest request = request(
                List.of(field("name", FieldRole.PRIORITY_F1)),
                List.of()
        );

        assertThatThrownBy(() -> configService.update(session, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Exactly one field must have ANCHOR");
    }

    @Test
    void rejectsConfigWithTwoAnchors() {
        MergeSession session = session(SessionStatus.FILES_UPLOADED);
        UpdateConfigRequest request = request(
                List.of(
                        field("name", FieldRole.ANCHOR),
                        field("status", FieldRole.ANCHOR)
                ),
                List.of()
        );

        assertThatThrownBy(() -> configService.update(session, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Exactly one field must have ANCHOR");
    }

    @Test
    void rejectsConfigWhenAnchorFieldDoesNotMatch() {
        MergeSession session = session(SessionStatus.FILES_UPLOADED);
        UpdateConfigRequest request = new UpdateConfigRequest(
                List.of(field("name", FieldRole.ANCHOR)),
                new AnchorSettingsDto("other", true, true),
                List.of()
        );

        assertThatThrownBy(() -> configService.update(session, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("anchorField must match");
    }

    @Test
    void rejectsPriorityFieldsOnAnchor() {
        MergeSession session = session(SessionStatus.FILES_UPLOADED);
        UpdateConfigRequest request = request(
                List.of(
                        new FieldRuleDto("name", FieldRole.ANCHOR, PriorityMode.ALWAYS, null, false)
                ),
                List.of()
        );

        assertThatThrownBy(() -> configService.update(session, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("only allowed for PRIORITY");
    }

    @Test
    void rejectsNewFieldConflictingWithExisting() {
        MergeSession session = session(SessionStatus.FILES_UPLOADED);
        UpdateConfigRequest request = request(
                List.of(field("name", FieldRole.ANCHOR)),
                List.of(new NewFieldDto("name", null))
        );

        assertThatThrownBy(() -> configService.update(session, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("conflicts with an existing field");
    }

    @Test
    void rejectsUpdateInWrongSessionState() {
        MergeSession session = session(SessionStatus.CREATED);
        UpdateConfigRequest request = request(
                List.of(field("name", FieldRole.ANCHOR)),
                List.of()
        );

        assertThatThrownBy(() -> configService.update(session, request))
                .isInstanceOf(InvalidSessionStateException.class);
    }

    // --- helpers ---

    private MergeSession session(SessionStatus status) {
        MergeSession session = new MergeSession("test-id", Instant.now());
        session.setStatus(status);
        return session;
    }

    private UpdateConfigRequest request(List<FieldRuleDto> fields, List<NewFieldDto> newFields) {
        return new UpdateConfigRequest(fields, new AnchorSettingsDto("name", true, true), newFields);
    }

    private FieldRuleDto field(String name, FieldRole role) {
        PriorityMode mode = (role == FieldRole.PRIORITY_F1 || role == FieldRole.PRIORITY_F2)
                ? PriorityMode.ALWAYS
                : null;
        return new FieldRuleDto(name, role, mode, null, false);
    }
}