package com.etu.countdown.gui;

import com.etu.countdown.BaseClass;
import com.etu.countdown.TimerType;
import com.etu.countdown.commands.Etkinlikler;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import java.util.List;

public class GUIListener implements Listener {
    private final BaseClass plugin;
    private final GUIManager guiManager;

    public GUIListener(BaseClass plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.startsWith(ChatColor.DARK_PURPLE + "Etkinlik Yönetimi")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;

            if (clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Yeni Etkinlik Ekle")) {
                player.openInventory(guiManager.createNewEventMenu());
                return;
            }

            List<String> lore = clickedItem.getItemMeta().getLore();
            if (lore != null && !lore.isEmpty()) {
                String eventId = ChatColor.stripColor(lore.get(lore.size() - 1)).substring(4);
                player.openInventory(guiManager.createEventMenu(eventId));
            }

            if (clickedItem.getItemMeta().getDisplayName().equals(ChatColor.LIGHT_PURPLE + "Discord Webhook")) {
                player.openInventory(guiManager.createWebhookMenu());
                return;
            }
        }
        else if (title.equals(ChatColor.DARK_PURPLE + "Yeni Etkinlik Oluştur")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;

            Calendar cal = Calendar.getInstance();
            @SuppressWarnings("unused")
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MM yyyy HH mm", new Locale("tr"));
            String eventBase = "etkinlik_" + System.currentTimeMillis();

            String itemName = clickedItem.getItemMeta().getDisplayName();
            if (itemName.equals(ChatColor.YELLOW + "Geri Dön")) {
                player.openInventory(guiManager.createMainMenu());
                return;
            }

            if (itemName.equals(ChatColor.GOLD + "Bugün")) {
                cal.add(Calendar.HOUR, 2);
                createQuickEvent(player, eventBase + "_bugun", cal.getTime(), true);
            } else if (itemName.equals(ChatColor.GOLD + "Yarın")) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 15);
                cal.set(Calendar.MINUTE, 0);
                createQuickEvent(player, eventBase + "_yarin", cal.getTime(), true);
            } else if (itemName.equals(ChatColor.GOLD + "Gelecek Hafta")) {
                cal.add(Calendar.WEEK_OF_YEAR, 1);
                cal.set(Calendar.HOUR_OF_DAY, 15);
                cal.set(Calendar.MINUTE, 0);
                createQuickEvent(player, eventBase + "_haftaya", cal.getTime(), true);
            } else if (itemName.equals(ChatColor.GREEN + "Özel Tarih")) {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Özel tarih için komutu kullan:");
                player.sendMessage(ChatColor.WHITE + "/editor add <isim> <gün> <ay> <yıl> <saat> <dakika>");
            } else if (itemName.equals(ChatColor.AQUA + "Hazır Şablon")) {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Önce tarihi belirle:");
                player.sendMessage(ChatColor.WHITE + "/editor add <isim> <tarih>");
                player.sendMessage(ChatColor.YELLOW + "Şablon otomatik eklenecek!");
            }
        }
        else if (title.startsWith(ChatColor.DARK_PURPLE + "Etkinlik: ")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;

            String eventId = title.substring(title.indexOf("ID: ") + 4, title.length() - 1);
            String itemName = clickedItem.getItemMeta().getDisplayName();

            if (itemName.equals(ChatColor.YELLOW + "Geri Dön")) {
                player.openInventory(guiManager.createMainMenu());
            }
            else if (itemName.equals(ChatColor.GREEN + "Yeni Zamanlayıcı Ekle")) {
                TimerType type = event.isRightClick() ? TimerType.TITLE : TimerType.MESSAGE;
                player.openInventory(guiManager.createTimerCreationMenu(eventId, type));
            }
            else if (itemName.equals(ChatColor.RED + "Etkinliği Sil")) {
                plugin.removeCountdown(eventId);
                plugin.refreshCountdowns();
                player.openInventory(guiManager.createMainMenu());
                player.sendMessage(ChatColor.GREEN + "Etkinlik silindi: " + eventId);
            }
            else if (event.getSlot() < 45 && clickedItem.getItemMeta().hasLore()) {
                int timerIndex = event.getSlot();
                plugin.removeTimer(eventId, timerIndex);
                player.closeInventory();
                
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    plugin.refreshCountdowns();
                    player.openInventory(guiManager.createEventMenu(eventId));
                }, 2L);
                
                player.sendMessage(ChatColor.GREEN + "Zamanlayıcı silindi: " + itemName);
            }
        }
        else if (title.startsWith(ChatColor.DARK_PURPLE + "Zamanlayıcı Ekle: ")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;

            String fullTitle = title.substring(("Zamanlayıcı Ekle: ").length());
            String eventId = fullTitle.contains("(ID: ") ? 
                fullTitle.substring(fullTitle.indexOf("(ID: ") + 5, fullTitle.indexOf(")")) : 
                fullTitle;

            String itemName = clickedItem.getItemMeta().getDisplayName();

            if (itemName.equals(ChatColor.YELLOW + "Geri Dön")) {
                player.openInventory(guiManager.createEventMenu(eventId));
                return;
            }

            TimerType type = title.contains("(Başlık)") ? TimerType.TITLE : TimerType.MESSAGE;
            
            if (itemName.equals(ChatColor.GREEN + "Özel Süre")) {
                player.closeInventory();
                plugin.getTimerCreationManager().startCustomTimeInput(player, eventId, type);
                return;
            }

            if (clickedItem.getType().toString().contains("CONCRETE")) {
                String timeStr = ChatColor.stripColor(itemName).split(" ")[0];
                int seconds = parseTimeString(timeStr);
                player.closeInventory();
                plugin.getTimerCreationManager().startContentInput(player, eventId, type, seconds);
            }
        }
        else if (title.equals(ChatColor.DARK_PURPLE + "Yaklaşan Etkinlikler")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;

            String itemName = clickedItem.getItemMeta().getDisplayName();
            
            if (itemName.equals(ChatColor.GREEN + "Yenile")) {
                plugin.refreshCountdowns();
                Etkinlikler etkinlikler = (Etkinlikler) plugin.getCommand("etkinlikler").getExecutor();
                player.openInventory(etkinlikler.openEventsGUI(player));
                return;
            }

            if (clickedItem.getType() == Material.BOOK) {
                if (!player.hasPermission("countdown.admin")) {
                    player.sendMessage(ChatColor.RED + "Bu etkinliği düzenlemek için yetkiniz yok!");
                    return;
                }
                
                List<String> lore = clickedItem.getItemMeta().getLore();
                String eventId = ChatColor.stripColor(lore.get(lore.size() - 1)).substring(4);
                player.openInventory(guiManager.createEventMenu(eventId));
            }
        }
        else if (title.equals(ChatColor.DARK_PURPLE + "Discord Webhook Ayarları")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;

            String itemName = clickedItem.getItemMeta().getDisplayName();
            
            if (itemName.equals(ChatColor.YELLOW + "Geri Dön")) {
                player.openInventory(guiManager.createMainMenu());
            }
            else if (itemName.equals(ChatColor.AQUA + "Webhook URL Ayarla")) {
                player.closeInventory();
                plugin.getTimerCreationManager().startWebhookConfiguration(player);
            }
            else if (itemName.startsWith(ChatColor.GREEN + "✔ Webhook Aktif") || 
                     itemName.startsWith(ChatColor.RED + "✘ Webhook Devre Dışı")) {
                boolean newState = !plugin.isWebhookEnabled();
                plugin.setWebhookEnabled(newState);
                player.openInventory(guiManager.createWebhookMenu());
            }
            else if (itemName.equals(ChatColor.GOLD + "Test Mesajı Gönder")) {
                if (!plugin.isWebhookEnabled()) {
                    player.sendMessage(ChatColor.RED + "Webhook aktif değil!");
                    return;
                }
                plugin.sendWebhook("Test mesajı - " + player.getName() + " tarafından gönderildi");
                player.sendMessage(ChatColor.GREEN + "Test mesajı gönderildi!");
            }
        }
    }

    private void createQuickEvent(Player player, String name, Date date, boolean addTemplate) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm", new Locale("tr"));
            String dateStr = dateFormat.format(date);
            String displayName = "Etkinlik " + dateStr;
            plugin.addCountdown(name, displayName, dateStr);

            if (addTemplate) {
                plugin.addTimer(name, 3600, TimerType.MESSAGE, "&e&lEtkinliğe 1 saat kaldı!");
                plugin.addTimer(name, 1800, TimerType.MESSAGE, "&e&lEtkinliğe 30 dakika kaldı!");
                plugin.addTimer(name, 600, TimerType.TITLE, "&6&l10 DAKİKA KALDI!");
                plugin.addTimer(name, 300, TimerType.MESSAGE, "&c&lEtkinliğe 5 dakika kaldı!");
                plugin.addTimer(name, 60, TimerType.TITLE, "&c&l1 DAKİKA KALDI!");
                plugin.addTimer(name, 0, TimerType.TITLE, "&4&lETKİNLİK BAŞLADI!");
            }

            player.sendMessage(ChatColor.GREEN + "Etkinlik oluşturuldu: " + dateStr);
            player.openInventory(guiManager.createEventMenu(name));
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Hata: " + e.getMessage());
        }
    }

    private int parseTimeString(String timeStr) {
        int amount = Integer.parseInt(timeStr.substring(0, timeStr.length() - 1));
        char unit = timeStr.charAt(timeStr.length() - 1);
        switch (unit) {
            case 's': return amount;
            case 'm': return amount * 60;
            case 'h': return amount * 3600;
            default: return amount;
        }
    }
} 