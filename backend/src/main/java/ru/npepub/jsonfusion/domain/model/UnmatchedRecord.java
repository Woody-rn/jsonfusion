package ru.npepub.jsonfusion.domain.model;

/**
 * A record that did not match a counterpart. Immutable; label and ignored
 * are set at creation time and replaced by creating a new instance if needed.
 */
public record UnmatchedRecord(
        String id,
        JsonRecord record,
        UnmatchedType type,
        String label,
        boolean ignored
) {

    public UnmatchedRecord withLabel(String newLabel) {
        return new UnmatchedRecord(id, record, type, newLabel, ignored);
    }

    public UnmatchedRecord withIgnored(boolean newIgnored) {
        return new UnmatchedRecord(id, record, type, label, newIgnored);
    }
}