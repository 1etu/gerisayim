package com.etu.countdown.discord;

import com.etu.countdown.BaseClass;
import org.bukkit.ChatColor;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhookManager {
    private final BaseClass plugin;
    private String webhookUrl;
    private boolean useWebhook;
    private boolean useEmbeds;
    private String embedColor;

    public DiscordWebhookManager(BaseClass plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        webhookUrl = plugin.getConfig().getString("discord.webhook_url", "");
        useWebhook = plugin.getConfig().getBoolean("discord.use_webhook", false);
        useEmbeds = plugin.getConfig().getBoolean("discord.use_embeds", false);
        embedColor = plugin.getConfig().getString("discord.embed_color", "yesil");
    }

    public void sendMessage(String content) {
        if (!useWebhook || webhookUrl.isEmpty()) {
            return;
        }

        try {
            JSONObject json = new JSONObject();
            String strippedContent = ChatColor.stripColor(content);

            if (useEmbeds) {
                JSONObject embed = new JSONObject();
                embed.put("description", strippedContent);
                embed.put("color", DiscordColorUtil.getColor(embedColor));

                JSONArray embeds = new JSONArray();
                embeds.put(embed);
                json.put("embeds", embeds);
            } else {
                json.put("content", strippedContent);
            }

            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            conn.disconnect();
            
            if (responseCode != 204) {
                plugin.getLogger().warning("dwe: " + responseCode);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("dwe: " + e.getMessage());
        }
    }

    public boolean isEnabled() {
        return useWebhook;
    }

    public void setEnabled(boolean enabled) {
        this.useWebhook = enabled;
        plugin.getConfig().set("discord.use_webhook", enabled);
        plugin.saveConfig();
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String url) {
        this.webhookUrl = url;
        plugin.getConfig().set("discord.webhook_url", url);
        plugin.saveConfig();
    }

    public boolean isUsingEmbeds() {
        return useEmbeds;
    }

    public void setUseEmbeds(boolean useEmbeds) {
        this.useEmbeds = useEmbeds;
        plugin.getConfig().set("discord.use_embeds", useEmbeds);
        plugin.saveConfig();
    }

    public String getEmbedColor() {
        return embedColor;
    }

    public void setEmbedColor(String color) {
        if (DiscordColorUtil.isValidColor(color)) {
            this.embedColor = color;
            plugin.getConfig().set("discord.embed_color", color);
            plugin.saveConfig();
        }
    }

    public boolean isValidEmbedColor(String color) {
        return DiscordColorUtil.isValidColor(color);
    }
} 