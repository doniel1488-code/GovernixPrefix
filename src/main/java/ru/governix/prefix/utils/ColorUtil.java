package ru.governix.prefix.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class ColorUtil {

    private static final LegacyComponentSerializer L = LegacyComponentSerializer.legacyAmpersand();

    private ColorUtil() {}

    public static Component color(String input) {
        if (input == null || input.isEmpty()) return Component.empty();
        return L.deserialize(input);
    }

    /**
     * Убирает все цветовые коды и оставляет только текст.
     */
    public static String stripAll(String input) {
        if (input == null) return "";
        return input
                .replaceAll("&#[A-Fa-f0-9]{6}", "")
                .replaceAll("&[0-9a-fk-orA-FK-OR]", "");
    }

    /**
     * Проверка на наличие &-цветов в строке.
     */
    public static boolean hasLegacyColors(String input) {
        if (input == null) return false;
        return input.matches(".*&[0-9a-fA-F].*");
    }

    /**
     * Проверка на наличие HEX (&#RRGGBB).
     */
    public static boolean hasHexColors(String input) {
        if (input == null) return false;
        return input.matches(".*&#[A-Fa-f0-9]{6}.*");
    }

    /**
     * Проверка на наличие градиента (&gradient[...]).
     */
    public static boolean hasGradient(String input) {
        if (input == null) return false;
        return input.toLowerCase().contains("&gradient[");
    }

    /**
     * Проверка на наличие &l (жирный).
     */
    public static boolean hasBold(String input) {
        if (input == null) return false;
        return input.toLowerCase().contains("&l");
    }
}
