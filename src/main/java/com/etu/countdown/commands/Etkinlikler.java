package com.etu.countdown.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.etu.countdown.BaseClass;
import com.etu.countdown.Timer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Etkinlikler implements CommandExecutor {
    private final BaseClass plugin;

    public Etkinlikler(BaseClass plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Bu komutu sadece oyuncular kullanabilir!");
            return true;
        }

        Player player = (Player) sender;
        player.openInventory(openEventsGUI(player));
        return true;
    }

    @SuppressWarnings("deprecation")
    public Inventory openEventsGUI(Player player) {
        plugin.refreshCountdowns();
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Yaklaşan Etkinlikler");
        Map<String, Date> countdowns = plugin.getCountdowns();

        int slot = 0;
        Date now = new Date();

        List<Map.Entry<String, Date>> sortedEvents = new ArrayList<>(countdowns.entrySet());
        sortedEvents.sort((a, b) -> a.getValue().compareTo(b.getValue()));

        for (Map.Entry<String, Date> entry : sortedEvents) {
            if (slot >= 45) break;

            String eventId = entry.getKey();
            String displayName = plugin.getDisplayName(eventId);
            Date eventDate = entry.getValue();
            long diff = eventDate.getTime() - now.getTime();

            if (diff > 0) {
                ItemStack item = new ItemStack(Material.BOOK);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + displayName);

                List<String> lore = new ArrayList<>();
                long days = TimeUnit.MILLISECONDS.toDays(diff);
                long hours = TimeUnit.MILLISECONDS.toHours(diff) % 24;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;

                lore.add("");
                lore.add(ChatColor.YELLOW + "⌚ " + ChatColor.GRAY + "Kalan süre:");
                if (days > 0) {
                    lore.add(ChatColor.WHITE + "  ▪ " + ChatColor.GOLD + days + ChatColor.GRAY + " gün");
                }
                if (hours > 0 || days > 0) {
                    lore.add(ChatColor.WHITE + "  ▪ " + ChatColor.GOLD + hours + ChatColor.GRAY + " saat");
                }
                lore.add(ChatColor.WHITE + "  ▪ " + ChatColor.GOLD + minutes + ChatColor.GRAY + " dakika");
                
                List<Timer> eventTimers = plugin.getTimers(eventId);
                if (!eventTimers.isEmpty()) {
                    lore.add("");
                    lore.add(ChatColor.YELLOW + "⚡ " + ChatColor.GRAY + "Zamanlayıcılar:");
                    for (int i = 0; i < Math.min(3, eventTimers.size()); i++) {
                        Timer timer = eventTimers.get(i);
                        String timeStr;
                        if (timer.getSeconds() >= 3600) {
                            timeStr = (timer.getSeconds() / 3600) + " saat";
                        } else if (timer.getSeconds() >= 60) {
                            timeStr = (timer.getSeconds() / 60) + " dakika";
                        } else {
                            timeStr = timer.getSeconds() + " saniye";
                        }
                        lore.add(ChatColor.WHITE + "  ▪ " + ChatColor.GRAY + timeStr + " kala");
                    }
                    if (eventTimers.size() > 3) {
                        lore.add(ChatColor.GRAY + "  ve " + (eventTimers.size() - 3) + " tane daha...");
                    }
                }

                lore.add("");
                lore.add(ChatColor.DARK_GRAY + "ID: " + eventId);

                meta.setLore(lore);
                item.setItemMeta(meta);
                gui.setItem(slot++, item);
            }
        }

        ItemStack separator = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1, (short) 8);
        ItemMeta separatorMeta = separator.getItemMeta();
        separatorMeta.setDisplayName(" ");
        separator.setItemMeta(separatorMeta);

        for (int i = slot; i < 45; i++) {
            gui.setItem(i, separator);
        }

        ItemStack refresh = new ItemStack(Material.EMERALD);
        ItemMeta refreshMeta = refresh.getItemMeta();
        refreshMeta.setDisplayName(ChatColor.GREEN + "Yenile");
        List<String> refreshLore = new ArrayList<>();
        refreshLore.add(ChatColor.GRAY + "Etkinlik listesini");
        refreshLore.add(ChatColor.GRAY + "güncellemek için tıkla");
        refreshMeta.setLore(refreshLore);
        refresh.setItemMeta(refreshMeta);
        gui.setItem(49, refresh);

        return gui;
    }
}