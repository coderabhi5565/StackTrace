package com.stacktrace.post_service.service;

import java.util.regex.Pattern;

/** Plain-text helpers for markdown content: excerpts and reading time. */
public final class MarkdownText {

    private static final Pattern CODE_FENCE = Pattern.compile("(?s)```.*?```");
    private static final Pattern WS = Pattern.compile("\\s+");

    private MarkdownText() {
    }

    public static String toPlainText(String markdown) {
        String s = CODE_FENCE.matcher(markdown).replaceAll(" ");
        s = s.replaceAll("`([^`]*)`", "$1")
                .replaceAll("!\\[[^]]*]\\([^)]*\\)", " ")
                .replaceAll("\\[([^]]*)]\\([^)]*\\)", "$1")
                .replaceAll("(?m)^\\s{0,3}(#{1,6}|>|[-*+]|\\d+\\.)\\s+", "")
                .replaceAll("[*_~]{1,3}", "")
                .replaceAll("<[^>]+>", " ");
        return WS.matcher(s).replaceAll(" ").trim();
    }

    public static String excerpt(String markdown, int max) {
        String text = toPlainText(markdown);
        if (text.length() <= max) {
            return text;
        }
        String cut = text.substring(0, max - 1);
        int lastSpace = cut.lastIndexOf(' ');
        if (lastSpace > max / 2) {
            cut = cut.substring(0, lastSpace);
        }
        return cut + "…";
    }

    public static int readingTimeMinutes(String markdown) {
        String trimmed = markdown.trim();
        int words = trimmed.isEmpty() ? 0 : WS.split(trimmed).length;
        return Math.max(1, (int) Math.ceil(words / 200.0));
    }
}
