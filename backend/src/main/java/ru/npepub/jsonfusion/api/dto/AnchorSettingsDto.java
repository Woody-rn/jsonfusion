package ru.npepub.jsonfusion.api.dto;

import ru.npepub.jsonfusion.domain.model.config.ComparisonSettings;
import ru.npepub.jsonfusion.domain.model.config.MergeConfig;

/**
 * Anchor field and comparison settings.
 */
public record AnchorSettingsDto(
        String anchorField,
        boolean ignoreCase,
        boolean ignoreExtraSpaces
) {

    public static AnchorSettingsDto from(MergeConfig config) {
        ComparisonSettings s = config.comparisonSettings();
        return new AnchorSettingsDto(
                config.anchorField(),
                s.ignoreCase(),
                s.ignoreExtraSpaces()
        );
    }
}