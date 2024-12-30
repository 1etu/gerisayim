package com.etu.countdown.gui;

import com.etu.countdown.BaseClass;
import com.etu.countdown.Timer;
import com.etu.countdown.TimerType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.*;

public class GUIManager {
    private final BaseClass plugin;
    private final SimpleDateFormat dateFormat;

    public GUIManager(BaseClass plugin) {
        this.plugin = plugin;
        this.dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm", new Locale("tr"));
    }

    public Inventory createMainMenu() {
        plugin.refreshCountdowns();
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Etkinlik Yönetimi");
        
        Map<String, Date> countdowns = plugin.getCountdowns();
        int slot = 0;
        
        Date now = new Date();

        for (Map.Entry<String, Date> entry : countdowns.entrySet()) {
            String eventId = entry.getKey();
            String displayName = plugin.getDisplayName(eventId);
            Date eventDate = entry.getValue();
            long diff = eventDate.getTime() - now.getTime();

            if (diff > 0) {
                ItemStack item = new ItemStack(Material.BOOK);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + displayName);
                
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GRAY + "Tarih: " + ChatColor.WHITE + dateFormat.format(entry.getValue()));
                lore.add("");
                
                List<Timer> eventTimers = plugin.getTimers(eventId);
                lore.add(ChatColor.YELLOW + "Zamanlayıcılar (" + eventTimers.size() + "):");
                
                for (Timer timer : eventTimers) {
                    String timeStr;
                    if (timer.getSeconds() >= 3600) {
                        timeStr = (timer.getSeconds() / 3600) + " saat";
                    } else if (timer.getSeconds() >= 60) {
                        timeStr = (timer.getSeconds() / 60) + " dakika";
                    } else {
                        timeStr = timer.getSeconds() + " saniye";
                    }
                    
                    lore.add(ChatColor.GRAY + "• " + timeStr + " - " + 
                            (timer.getType() == TimerType.MESSAGE ? "Mesaj" : "Başlık"));
                }
                
                lore.add("");
                lore.add(ChatColor.GREEN + "→ Tıkla ve düzenle");
                lore.add(ChatColor.DARK_GRAY + "ID: " + eventId);
                
                meta.setLore(lore);
                item.setItemMeta(meta);
                gui.setItem(slot++, item);
            }
        }

        ItemStack addNew = new ItemStack(Material.EMERALD);
        ItemMeta addMeta = addNew.getItemMeta();
        addMeta.setDisplayName(ChatColor.GREEN + "Yeni Etkinlik Ekle");
        List<String> addLore = new ArrayList<>();
        addLore.add(ChatColor.GRAY + "Yeni bir etkinlik oluşturmak");
        addLore.add(ChatColor.GRAY + "için tıklayın");
        addMeta.setLore(addLore);
        addNew.setItemMeta(addMeta);
        gui.setItem(53, addNew);

        ItemStack webhook = new ItemStack(plugin.isWebhookEnabled() ? Material.GREEN_WOOL : Material.RED_WOOL);
        ItemMeta webhookMeta = webhook.getItemMeta();
        webhookMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Discord Webhook");
        List<String> webhookLore = new ArrayList<>();
        webhookLore.add(ChatColor.GRAY + "Discord webhook ayarlarını");
        webhookLore.add(ChatColor.GRAY + "düzenlemek için tıkla");
        webhookLore.add("");
        webhookLore.add(ChatColor.GRAY + "Durum: " + 
            (plugin.isWebhookEnabled() ? ChatColor.GREEN + "Aktif" : ChatColor.RED + "Kapalı"));
        if (plugin.isWebhookEnabled()) {
            webhookLore.add(ChatColor.GRAY + "URL: " + ChatColor.WHITE + plugin.getWebhookUrl());
        }
        webhookMeta.setLore(webhookLore);
        webhook.setItemMeta(webhookMeta);
        gui.setItem(52, webhook);
        
        return gui;
    }

    public Inventory createEventMenu(String eventId) {
        plugin.refreshCountdowns();
        String displayName = plugin.getDisplayName(eventId);
        
        Inventory gui = Bukkit.createInventory(null, 54, 
            ChatColor.DARK_PURPLE + "Etkinlik: " + displayName + " (ID: " + eventId + ")");
        
        gui.clear();
        
        List<Timer> timers = new ArrayList<>(plugin.getTimers(eventId));
        timers.sort((t1, t2) -> Integer.compare(t2.getSeconds(), t1.getSeconds()));
        
        int slot = 0;
        for (Timer timer : timers) {
            ItemStack item = new ItemStack(timer.getType() == TimerType.MESSAGE ? Material.PAPER : Material.MAP);
            ItemMeta meta = item.getItemMeta();
            
            String timeStr;
            if (timer.getSeconds() >= 3600) {
                timeStr = (timer.getSeconds() / 3600) + " saat";
            } else if (timer.getSeconds() >= 60) {
                timeStr = (timer.getSeconds() / 60) + " dakika";
            } else {
                timeStr = timer.getSeconds() + " saniye";
            }
            
            meta.setDisplayName(ChatColor.YELLOW + timeStr + " önce");
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Tip: " + ChatColor.WHITE + 
                    (timer.getType() == TimerType.MESSAGE ? "Mesaj" : "Başlık"));
            lore.add(ChatColor.GRAY + "İçerik: " + ChatColor.WHITE + timer.getContent());
            lore.add("");
            lore.add(ChatColor.RED + "→ Silmek için tıkla");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
            
            gui.setItem(slot++, item);
        }

        ItemStack addTimer = new ItemStack(Material.EMERALD);
        ItemMeta addMeta = addTimer.getItemMeta();
        addMeta.setDisplayName(ChatColor.GREEN + "Yeni Zamanlayıcı Ekle");
        List<String> addLore = new ArrayList<>();
        addLore.add(ChatColor.GRAY + "Yeni bir zamanlayıcı");
        addLore.add(ChatColor.GRAY + "eklemek için tıkla");
        addLore.add("");
        addLore.add(ChatColor.WHITE + "→ Sol tık: Mesaj zamanlayıcısı");
        addLore.add(ChatColor.WHITE + "→ Sağ tık: Başlık zamanlayıcısı");
        addLore.add(ChatColor.WHITE + "→ Shift + Sağ tık: Komut zamanlayıcısı");
        addMeta.setLore(addLore);
        addTimer.setItemMeta(addMeta);
        gui.setItem(53, addTimer);
        
        ItemStack deleteEvent = new ItemStack(Material.BARRIER);
        ItemMeta deleteMeta = deleteEvent.getItemMeta();
        deleteMeta.setDisplayName(ChatColor.RED + "Etkinliği Sil");
        List<String> deleteLore = new ArrayList<>();
        deleteLore.add(ChatColor.GRAY + "Bu etkinliği silmek için tıkla");
        deleteLore.add("");
        deleteLore.add(ChatColor.RED + "⚠ Bu işlem geri alınamaz!");
        deleteMeta.setLore(deleteLore);
        deleteEvent.setItemMeta(deleteMeta);
        gui.setItem(49, deleteEvent);
        
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "Geri Dön");
        back.setItemMeta(backMeta);
        gui.setItem(45, back);
        
        return gui;
    }

    public Inventory createNewEventMenu() {
        plugin.refreshCountdowns();
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Yeni Etkinlik Oluştur");
        
        ItemStack today = new ItemStack(Material.SUNFLOWER);
        ItemMeta todayMeta = today.getItemMeta();
        todayMeta.setDisplayName(ChatColor.GOLD + "Bugün");
        List<String> todayLore = new ArrayList<>();
        todayLore.add(ChatColor.GRAY + "Bugün için bir etkinlik");
        todayLore.add(ChatColor.GRAY + "oluşturmak için tıkla");
        todayMeta.setLore(todayLore);
        today.setItemMeta(todayMeta);
        gui.setItem(10, today);

        ItemStack tomorrow = new ItemStack(Material.CLOCK);
        ItemMeta tomorrowMeta = tomorrow.getItemMeta();
        tomorrowMeta.setDisplayName(ChatColor.GOLD + "Yarın");
        List<String> tomorrowLore = new ArrayList<>();
        tomorrowLore.add(ChatColor.GRAY + "Yarın için bir etkinlik");
        tomorrowLore.add(ChatColor.GRAY + "oluşturmak için tıkla");
        tomorrowMeta.setLore(tomorrowLore);
        tomorrow.setItemMeta(tomorrowMeta);
        gui.setItem(11, tomorrow);

        ItemStack nextWeek = new ItemStack(Material.COMPASS);
        ItemMeta nextWeekMeta = nextWeek.getItemMeta();
        nextWeekMeta.setDisplayName(ChatColor.GOLD + "Gelecek Hafta");
        List<String> nextWeekLore = new ArrayList<>();
        nextWeekLore.add(ChatColor.GRAY + "Gelecek hafta için bir");
        nextWeekLore.add(ChatColor.GRAY + "etkinlik oluşturmak için tıkla");
        nextWeekMeta.setLore(nextWeekLore);
        nextWeek.setItemMeta(nextWeekMeta);
        gui.setItem(12, nextWeek);

        ItemStack customDate = new ItemStack(Material.BOOK);
        ItemMeta customDateMeta = customDate.getItemMeta();
        customDateMeta.setDisplayName(ChatColor.GREEN + "Özel Tarih");
        List<String> customDateLore = new ArrayList<>();
        customDateLore.add(ChatColor.GRAY + "İstediğin bir tarih için");
        customDateLore.add(ChatColor.GRAY + "etkinlik oluşturmak için tıkla");
        customDateLore.add("");
        customDateLore.add(ChatColor.YELLOW + "Komut formatı:");
        customDateLore.add(ChatColor.WHITE + "/editor add <isim> <gün> <ay> <yıl> <saat> <dakika>");
        customDateLore.add(ChatColor.GRAY + "Örnek: /editor add Parti 25 12 2024 15 30");
        customDateMeta.setLore(customDateLore);
        customDate.setItemMeta(customDateMeta);
        gui.setItem(14, customDate);

        ItemStack template = new ItemStack(Material.FILLED_MAP);
        ItemMeta templateMeta = template.getItemMeta();
        templateMeta.setDisplayName(ChatColor.AQUA + "Hazır Şablon");
        List<String> templateLore = new ArrayList<>();
        templateLore.add(ChatColor.GRAY + "Önceden hazırlanmış zamanlayıcı");
        templateLore.add(ChatColor.GRAY + "şablonları ile hızlıca oluştur");
        templateLore.add("");
        templateLore.add(ChatColor.WHITE + "Şablon içeriği:");
        templateLore.add(ChatColor.GRAY + "• 1 saat kala - Mesaj");
        templateLore.add(ChatColor.GRAY + "• 30 dakika kala - Mesaj");
        templateLore.add(ChatColor.GRAY + "• 10 dakika kala - Başlık");
        templateLore.add(ChatColor.GRAY + "• 5 dakika kala - Mesaj");
        templateLore.add(ChatColor.GRAY + "• 1 dakika kala - Başlık");
        templateLore.add(ChatColor.GRAY + "• Başlangıç - Başlık");
        templateMeta.setLore(templateLore);
        template.setItemMeta(templateMeta);
        gui.setItem(16, template);

        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.YELLOW + "Bilgi");
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ChatColor.GRAY + "Etkinlik oluşturduktan sonra:");
        infoLore.add(ChatColor.WHITE + "1. Zamanlayıcıları ekleyebilirsin");
        infoLore.add(ChatColor.WHITE + "2. İstediğin zaman düzenleyebilirsin");
        infoLore.add(ChatColor.WHITE + "3. Tüm oyuncular görebilir");
        infoLore.add("");
        infoLore.add(ChatColor.GRAY + "İpucu: Şablonu kullanarak hızlıca");
        infoLore.add(ChatColor.GRAY + "zamanlayıcıları ekleyebilirsin!");
        infoMeta.setLore(infoLore);
        info.setItemMeta(infoMeta);
        gui.setItem(31, info);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "Geri Dön");
        back.setItemMeta(backMeta);
        gui.setItem(45, back);

        return gui;
    }

    public Inventory createTimerCreationMenu(String eventId, TimerType type) {
        plugin.refreshCountdowns();
        String displayName = plugin.getDisplayName(eventId);
        Inventory gui = Bukkit.createInventory(null, 54, 
            ChatColor.DARK_PURPLE + "Zamanlayıcı Ekle: " + displayName + " (ID: " + eventId + ")");
        
        String[] times = {"5s", "10s", "30s", "1m", "5m", "10m", "30m", "1h"};
        @SuppressWarnings("unused")
        int[] seconds = {5, 10, 30, 60, 300, 600, 1800, 3600};
        Material[] materials = {
            Material.RED_CONCRETE,
            Material.ORANGE_CONCRETE,
            Material.YELLOW_CONCRETE,
            Material.LIME_CONCRETE,
            Material.GREEN_CONCRETE,
            Material.CYAN_CONCRETE,
            Material.LIGHT_BLUE_CONCRETE,
            Material.BLUE_CONCRETE
        };
        
        for (int i = 0; i < times.length; i++) {
            ItemStack item = new ItemStack(materials[i]);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + times[i] + " önce");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Tıkla ve içerik gir");
            lore.add(ChatColor.GRAY + "Tip: " + (type == TimerType.MESSAGE ? "Mesaj" : "Başlık"));
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(10 + i + (i > 3 ? 5 : 0), item);
        }
        
        ItemStack custom = new ItemStack(Material.CLOCK);
        ItemMeta customMeta = custom.getItemMeta();
        customMeta.setDisplayName(ChatColor.GREEN + "Özel Süre");
        List<String> customLore = new ArrayList<>();
        customLore.add(ChatColor.GRAY + "İstediğin bir süre için");
        customLore.add(ChatColor.GRAY + "zamanlayıcı ekle");
        customMeta.setLore(customLore);
        custom.setItemMeta(customMeta);
        gui.setItem(31, custom);
        
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "Geri Dön");
        back.setItemMeta(backMeta);
        gui.setItem(45, back);
        
        return gui;
    }

    public Inventory createWebhookMenu() {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "Discord Webhook Ayarları");
        
        ItemStack status = new ItemStack(plugin.isWebhookEnabled() ? Material.GREEN_WOOL : Material.RED_WOOL);
        ItemMeta statusMeta = status.getItemMeta();
        statusMeta.setDisplayName((plugin.isWebhookEnabled() ? ChatColor.GREEN + "✔ Webhook Aktif" : 
            ChatColor.RED + "✘ Webhook Devre Dışı"));
        List<String> statusLore = new ArrayList<>();
        statusLore.add("");
        statusLore.add(ChatColor.GRAY + "Durum: " + 
            (plugin.isWebhookEnabled() ? ChatColor.GREEN + "Aktif" : ChatColor.RED + "Kapalı"));
        if (plugin.isWebhookEnabled() && !plugin.getWebhookUrl().isEmpty()) {
            statusLore.add("");
            statusLore.add(ChatColor.GRAY + "Webhook URL:");
            statusLore.add(ChatColor.WHITE + formatWebhookUrl(plugin.getWebhookUrl()));
        }
        statusLore.add("");
        statusLore.add(ChatColor.YELLOW + "→ Tıklayarak " + 
            (plugin.isWebhookEnabled() ? "devre dışı bırak" : "aktif et"));
        statusMeta.setLore(statusLore);
        status.setItemMeta(statusMeta);
        gui.setItem(13, status);
        
        ItemStack urlConfig = new ItemStack(Material.BOOK);
        ItemMeta urlMeta = urlConfig.getItemMeta();
        urlMeta.setDisplayName(ChatColor.AQUA + "Webhook URL Ayarla");
        List<String> urlLore = new ArrayList<>();
        urlLore.add(ChatColor.GRAY + "Discord webhook URL'sini");
        urlLore.add(ChatColor.GRAY + "ayarlamak için tıkla");
        if (!plugin.getWebhookUrl().isEmpty()) {
            urlLore.add("");
            urlLore.add(ChatColor.GRAY + "Mevcut URL:");
            urlLore.add(ChatColor.WHITE + formatWebhookUrl(plugin.getWebhookUrl()));
        }
        urlMeta.setLore(urlLore);
        urlConfig.setItemMeta(urlMeta);
        gui.setItem(11, urlConfig);
        
        ItemStack test = new ItemStack(Material.PAPER);
        ItemMeta testMeta = test.getItemMeta();
        testMeta.setDisplayName(ChatColor.GOLD + "Test Mesajı Gönder");
        List<String> testLore = new ArrayList<>();
        testLore.add(ChatColor.GRAY + "Webhook'un çalışıp çalışmadığını");
        testLore.add(ChatColor.GRAY + "test etmek için tıkla");
        if (!plugin.isWebhookEnabled()) {
            testLore.add("");
            testLore.add(ChatColor.RED + "Webhook aktif değil!");
        }
        testMeta.setLore(testLore);
        test.setItemMeta(testMeta);
        gui.setItem(15, test);
        
        ItemStack embedToggle = new ItemStack(Material.BOOK);
        ItemMeta embedMeta = embedToggle.getItemMeta();
        boolean usingEmbeds = plugin.getDiscordManager().isUsingEmbeds();
        embedMeta.setDisplayName((usingEmbeds ? ChatColor.GREEN + "✔ Embed Mesajlar" : 
            ChatColor.RED + "✘ Normal Mesajlar"));
        List<String> embedLore = new ArrayList<>();
        embedLore.add("");
        embedLore.add(ChatColor.GRAY + "Mesaj tipi: " + 
            (usingEmbeds ? ChatColor.GREEN + "Embed" : ChatColor.RED + "Normal"));
        if (usingEmbeds) {
            embedLore.add(ChatColor.GRAY + "Renk: " + ChatColor.WHITE + 
                plugin.getDiscordManager().getEmbedColor());
        }
        embedLore.add("");
        embedLore.add(ChatColor.YELLOW + "→ Tıklayarak değiştir");
        embedMeta.setLore(embedLore);
        embedToggle.setItemMeta(embedMeta);
        gui.setItem(14, embedToggle);
        
        if (usingEmbeds) {
            ItemStack colorPicker = new ItemStack(Material.CYAN_DYE);
            ItemMeta colorMeta = colorPicker.getItemMeta();
            colorMeta.setDisplayName(ChatColor.AQUA + "Embed Rengi");
            List<String> colorLore = new ArrayList<>();
            colorLore.add(ChatColor.GRAY + "Şu anki renk: " + ChatColor.WHITE + 
                plugin.getDiscordManager().getEmbedColor());
            colorLore.add("");
            colorLore.add(ChatColor.GRAY + "Kullanılabilir renkler:");
            colorLore.add(ChatColor.WHITE + "beyaz, siyah, kirmizi, yesil,");
            colorLore.add(ChatColor.WHITE + "mavi, sari, mor, turuncu,");
            colorLore.add(ChatColor.WHITE + "pembe, turkuaz");
            colorLore.add("");
            colorLore.add(ChatColor.YELLOW + "→ Tıklayarak değiştir");
            colorMeta.setLore(colorLore);
            colorPicker.setItemMeta(colorMeta);
            gui.setItem(16, colorPicker);
        }
        
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "Geri Dön");
        back.setItemMeta(backMeta);
        gui.setItem(18, back);
        
        return gui;
    }

    private String formatWebhookUrl(String url) {
        if (url.length() <= 40) return url;
        
        int idStart = url.indexOf("/webhooks/") + 10;
        int idEnd = url.indexOf("/", idStart);
        
        if (idStart != -1 && idEnd != -1) {
            String start = "https://discord.com/.../";
            String id = url.substring(idStart, idEnd);
            String token = url.substring(idEnd + 1);
            
            if (id.length() > 6) {
                id = id.substring(0, 3) + "..." + id.substring(id.length() - 3);
            }
            if (token.length() > 6) {
                token = token.substring(0, 3) + "..." + token.substring(token.length() - 3); 
            }
            
            return start + id + "/" + token;
        }
        
        return url.substring(0, 20) + "..." + url.substring(url.length() - 20);
    }
} 