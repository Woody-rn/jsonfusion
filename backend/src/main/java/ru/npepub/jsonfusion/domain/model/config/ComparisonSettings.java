package ru.npepub.jsonfusion.domain.model.config;

/**
 * Rules for comparing anchor values between two documents.
 */
public record ComparisonSettings(
        boolean ignoreCase,
        boolean ignoreExtraSpaces
) {
}