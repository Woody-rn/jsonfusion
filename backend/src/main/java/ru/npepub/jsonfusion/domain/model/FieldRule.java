package ru.npepub.jsonfusion.domain.model;

/**
 * Rule for a single field during merge.
 * {@code priorityMode}, {@code ifEqualsValue} and {@code ifEqualsNull}
 * are only relevant when role is PRIORITY_F1 or PRIORITY_F2.
 */
public record FieldRule(
        String fieldName,
        FieldRole role,
        PriorityMode priorityMode,
        String ifEqualsValue,
        boolean ifEqualsNull
) {

    public boolean isPriority() {
        return role == FieldRole.PRIORITY_F1 || role == FieldRole.PRIORITY_F2;
    }

    public boolean isAnchor() {
        return role == FieldRole.ANCHOR;
    }

    public boolean isDelete() {
        return role == FieldRole.DELETE;
    }
}