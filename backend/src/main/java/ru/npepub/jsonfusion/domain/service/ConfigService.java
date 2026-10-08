package ru.npepub.jsonfusion.domain.service;

import org.springframework.stereotype.Service;
import ru.npepub.jsonfusion.api.dto.AnchorSettingsDto;
import ru.npepub.jsonfusion.api.dto.NewFieldDto;
import ru.npepub.jsonfusion.api.dto.request.FieldRuleDto;
import ru.npepub.jsonfusion.api.dto.request.UpdateConfigRequest;
import ru.npepub.jsonfusion.domain.exception.InvalidSessionStateException;
import ru.npepub.jsonfusion.domain.exception.ValidationException;
import ru.npepub.jsonfusion.domain.model.config.ComparisonSettings;
import ru.npepub.jsonfusion.domain.model.config.FieldRule;
import ru.npepub.jsonfusion.domain.model.config.MergeConfig;
import ru.npepub.jsonfusion.domain.model.config.NewField;
import ru.npepub.jsonfusion.domain.model.session.MergeSession;
import ru.npepub.jsonfusion.domain.model.session.SessionStatus;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles reading and updating the merge configuration of a session.
 */
@Service
public class ConfigService {

    private final FieldsAnalyzerService analyzerService;
    private final SessionService sessionService;

    public ConfigService(FieldsAnalyzerService analyzerService, SessionService sessionService) {
        this.analyzerService = analyzerService;
        this.sessionService = sessionService;
    }

    /**
     * Builds the default config for a session. The session must be in FILES_UPLOADED state.
     */
    public MergeConfig buildDefault(MergeSession session) {
        if (session.getStatus() != SessionStatus.FILES_UPLOADED) {
            throw new InvalidSessionStateException(
                    "Cannot analyze fields: session is in " + session.getStatus() + " state"
            );
        }
        return analyzerService.analyze(session.getFile1(), session.getFile2());
    }

    /**
     * Applies a config update to a session. Validates and stores the new config.
     */
    public MergeConfig update(MergeSession session, UpdateConfigRequest request) {
        if (session.getStatus() != SessionStatus.FILES_UPLOADED
                && session.getStatus() != SessionStatus.CONFIGURED) {
            throw new InvalidSessionStateException(
                    "Cannot update config: session is in " + session.getStatus() + " state"
            );
        }

        MergeConfig config = toConfig(request);
        validate(config);

        session.setConfig(config);
        sessionService.updateStatus(session, SessionStatus.CONFIGURED);
        return config;
    }

    private MergeConfig toConfig(UpdateConfigRequest request) {
        List<FieldRule> rules = request.fields().stream()
                .map(FieldRuleDto::toDomain)
                .toList();

        List<NewField> newFields = request.newFields().stream()
                .map(NewFieldDto::toDomain)
                .toList();

        AnchorSettingsDto anchor = request.anchorSettings();
        ComparisonSettings settings = new ComparisonSettings(anchor.ignoreCase(), anchor.ignoreExtraSpaces());

        return new MergeConfig(rules, anchor.anchorField(), settings, newFields);
    }

    private void validate(MergeConfig config) {
        long anchorCount = config.fieldRules().stream().filter(FieldRule::isAnchor).count();
        if (anchorCount != 1) {
            throw new ValidationException("Exactly one field must have ANCHOR role, found: " + anchorCount);
        }

        boolean anchorMatches = config.fieldRules().stream()
                .anyMatch(r -> r.isAnchor() && r.fieldName().equals(config.anchorField()));
        if (!anchorMatches) {
            throw new ValidationException("anchorField must match the field with ANCHOR role");
        }

        for (FieldRule rule : config.fieldRules()) {
            if (!rule.isPriority() && (rule.priorityMode() != null || rule.ifEqualsValue() != null || rule.ifEqualsNull())) {
                throw new ValidationException(
                        "priorityMode/ifEqualsValue/ifEqualsNull are only allowed for PRIORITY_* fields: " + rule.fieldName()
                );
            }
        }

        Set<String> ruleNames = config.fieldRules().stream()
                .map(FieldRule::fieldName)
                .collect(Collectors.toSet());
        if (ruleNames.size() != config.fieldRules().size()) {
            throw new ValidationException("Field names must be unique");
        }

        Set<String> newFieldNames = config.newFields().stream()
                .map(NewField::name)
                .collect(Collectors.toSet());
        if (newFieldNames.size() != config.newFields().size()) {
            throw new ValidationException("New field names must be unique");
        }
        for (String name : newFieldNames) {
            if (ruleNames.contains(name)) {
                throw new ValidationException("New field '" + name + "' conflicts with an existing field");
            }
        }
    }
}