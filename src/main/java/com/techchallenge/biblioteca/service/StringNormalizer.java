package com.techchallenge.biblioteca.service;

import java.util.Locale;

public final class StringNormalizer {

    private StringNormalizer() {
    }

    public static String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    public static String normalizeEmail(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizeUpperCase(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
