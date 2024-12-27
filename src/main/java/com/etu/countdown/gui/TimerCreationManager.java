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

public class TimerCreationManager implements Listener {
    private final BaseClass plugin;
    private final Map<UUID, TimerCreationSession> sessions = new HashMap<>();

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

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        TimerCreationSession session = sessions.get(player.getUniqueId());
        
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
            sessions.remove(player.getUniqueId());
            
            String cleanEventId = eventId.split(" ")[0];
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.openInventory(plugin.getGuiManager().createEventMenu(cleanEventId));
                }, 1L);
            });
        }
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