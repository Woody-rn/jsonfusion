package ru.npepub.jsonfusion.domain.model;

/**
 * Role of a field during merge. Exactly one field per session has ANCHOR role.
 */
public enum FieldRole {
    ANCHOR,
    PRIORITY_F1,
    PRIORITY_F2,
    DELETE
}