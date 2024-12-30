package com.etu.countdown.gui;

import com.etu.countdown.BaseClass;
import com.etu.countdown.TimerType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

public class TimerCreationManager implements Listener {
    private final BaseClass plugin;
    private final Map<UUID, TimerCreationSession> sessions = new HashMap<>();
    private final Set<UUID> webhookConfigs = new HashSet<>();
    private final Set<UUID> embedColorConfigs = new HashSet<>();

    public TimerCreationManager(BaseClass plugin) {
        this.plugin = plugin;
    }

    public void startCustomTimeInput(Player player, String eventName, TimerType type) {
        sessions.put(player.getUniqueId(), new TimerCreationSession(eventName, type));
        player.sendMessage(ChatColor.YELLOW + "Kaç saniye önce tetiklensin? (Sadece sayı girin)");
    }

    public void startContentInput(Player player, String eventName, TimerType type, int seconds) {
        TimerCreationSession session = new TimerCreationSession(eventName, type);
        session.setSeconds(seconds);
        sessions.put(player.getUniqueId(), session);
        player.sendMessage(ChatColor.YELLOW + "Zamanlayıcı içeriğini girin:");
    }

    public void startWebhookConfiguration(Player player) {
        webhookConfigs.add(player.getUniqueId());
        player.sendMessage(ChatColor.YELLOW + "Discord webhook URL'sini girin:");
        player.sendMessage(ChatColor.GRAY + "İptal etmek için 'iptal' yazın");
    }

    public void startEmbedColorConfiguration(Player player) {
        embedColorConfigs.add(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        if (webhookConfigs.contains(playerId)) {
            event.setCancelled(true);
            String message = event.getMessage();
            
            if (message.equalsIgnoreCase("iptal")) {
                webhookConfigs.remove(playerId);
                player.sendMessage(ChatColor.RED + "Webhook yapılandırması iptal edildi.");
                return;
            }
            
            if (!isValidDiscordWebhook(message)) {
                player.sendMessage(ChatColor.RED + "Geçersiz Discord webhook URL'si!");
                player.sendMessage(ChatColor.RED + "URL örnekteki formatta olmalıdır:");
                player.sendMessage(ChatColor.GRAY + "https://discord.com/api/webhooks/[ID]/[TOKEN]");
                return;
            }
            
            plugin.setWebhookUrl(message);
            plugin.setWebhookEnabled(true);
            webhookConfigs.remove(playerId);
            
            player.sendMessage(ChatColor.GREEN + "Webhook URL'si kaydedildi ve aktif edildi!");
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.openInventory(plugin.getGuiManager().createWebhookMenu());
            });
            return;
        } else if (embedColorConfigs.contains(playerId)) {
            event.setCancelled(true);
            String message = event.getMessage();
            
            if (!plugin.getDiscordManager().isValidEmbedColor(message)) {
                player.sendMessage(ChatColor.RED + "Geçersiz renk! Kullanılabilir renkler:");
                player.sendMessage(ChatColor.GRAY + "beyaz, siyah, kirmizi, yesil, mavi, sari,");
                player.sendMessage(ChatColor.GRAY + "mor, turuncu, pembe, turkuaz");
                return;
            }
            
            plugin.getDiscordManager().setEmbedColor(message);
            embedColorConfigs.remove(playerId);
            
            player.sendMessage(ChatColor.GREEN + "Embed rengi ayarlandı: " + message);
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.openInventory(plugin.getGuiManager().createWebhookMenu());
            });
            return;
        } else {
            TimerCreationSession session = sessions.get(playerId);
            
            if (session == null) return;
            
            event.setCancelled(true);
            
            if (session.getSeconds() == 0) {
                try {
                    int seconds = Integer.parseInt(event.getMessage());
                    session.setSeconds(seconds);
                    player.sendMessage(ChatColor.YELLOW + "Zamanlayıcı içeriğini girin:");
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Lütfen geçerli bir sayı girin!");
                }
            } else {
                String content = event.getMessage();
                String eventId = session.getEventName();
                plugin.addTimer(eventId, session.getSeconds(), session.getType(), content);
                player.sendMessage(ChatColor.GREEN + "Zamanlayıcı eklendi!");
                sessions.remove(playerId);
                
                String cleanEventId = eventId.split(" ")[0];
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.closeInventory();
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        player.openInventory(plugin.getGuiManager().createEventMenu(cleanEventId));
                    }, 1L);
                });
            }
        }
    }

    private boolean isValidDiscordWebhook(String url) {
        return url.matches("https://discord\\.com/api/webhooks/\\d+/[\\w-]+");
    }

    private static class TimerCreationSession {
        private final String eventName;
        private final TimerType type;
        private int seconds;

        public TimerCreationSession(String eventName, TimerType type) {
            this.eventName = eventName;
            this.type = type;
            this.seconds = 0;
        }

        public String getEventName() { return eventName; }
        public TimerType getType() { return type; }
        public int getSeconds() { return seconds; }
        public void setSeconds(int seconds) { this.seconds = seconds; }
    }
} 