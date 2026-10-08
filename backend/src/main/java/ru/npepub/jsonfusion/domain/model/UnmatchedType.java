package ru.npepub.jsonfusion.domain.model;

/**
 * Reason why a record did not match a counterpart in the other document.
 */
public enum UnmatchedType {
    ONLY_IN_LEFT,
    ONLY_IN_RIGHT,
    DUPLICATE
}