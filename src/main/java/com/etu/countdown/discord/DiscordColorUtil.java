package com.etu.countdown.discord;

import java.util.HashMap;
import java.util.Map;

public class DiscordColorUtil {
    private static final Map<String, Integer> colorMap = new HashMap<>();

    static {
        colorMap.put("beyaz", 0xFFFFFF);
        colorMap.put("siyah", 0x000000);
        colorMap.put("kirmizi", 0xFF0000);
        colorMap.put("yesil", 0x00FF00);
        colorMap.put("mavi", 0x0000FF);
        colorMap.put("sari", 0xFFFF00);
        colorMap.put("mor", 0x9B59B6);
        colorMap.put("turuncu", 0xE67E22);
        colorMap.put("pembe", 0xFF69B4);
        colorMap.put("turkuaz", 0x1ABC9C);
    }

    public static int getColor(String colorName) {
        String normalizedName = colorName.toLowerCase().replace("ı", "i");
        return colorMap.getOrDefault(normalizedName, 0x00FF00); // def: yeşil
    }

    public static boolean isValidColor(String colorName) {
        String normalizedName = colorName.toLowerCase().replace("ı", "i");
        return colorMap.containsKey(normalizedName);
    }
} 