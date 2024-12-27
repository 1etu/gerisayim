package com.etu.countdown;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.etu.countdown.commands.Etkinlikler;
import com.etu.countdown.commands.Editor;
import com.etu.countdown.commands.TestCommand;
import com.etu.countdown.gui.GUIManager;
import com.etu.countdown.gui.GUIListener;
import com.etu.countdown.gui.TimerCreationManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaseClass extends JavaPlugin {
    private Map<String, Date> countdowns = new HashMap<>();
    private Map<String, List<Timer>> timers = new HashMap<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm", new Locale("tr"));
    private GUIManager guiManager;
    private TimerCreationManager timerCreationManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadCountdowns();
        guiManager = new GUIManager(this);
        timerCreationManager = new TimerCreationManager(this);
        getCommand("etkinlikler").setExecutor(new Etkinlikler(this));
        getCommand("editor").setExecutor(new Editor(this));
        getCommand("test").setExecutor(new TestCommand(this));
        getServer().getPluginManager().registerEvents(new GUIListener(this, guiManager), this);
        getServer().getPluginManager().registerEvents(timerCreationManager, this);
        startCountdownChecker();
    }

    public void loadCountdowns() {
        if (getConfig().contains("countdowns")) {
            for (String key : getConfig().getConfigurationSection("countdowns").getKeys(false)) {
                try {
                    String dateStr = getConfig().getString("countdowns." + key + ".date");
                    if (dateStr == null) {
                        getLogger().warning("ndfc: " + key);
                        continue; // geç
                    }
                    
                    Date date = dateFormat.parse(dateStr);
                    countdowns.put(key, date);
                    
                    List<Timer> eventTimers = new ArrayList<>();
                    if (getConfig().contains("countdowns." + key + ".timers")) {
                        for (Map<?, ?> timerMap : getConfig().getMapList("countdowns." + key + ".timers")) {
                            int seconds = 0;
                            
                            if (timerMap.containsKey("saniye")) {
                                seconds = Integer.parseInt(timerMap.get("saniye").toString());
                            } else if (timerMap.containsKey("dakika")) {
                                seconds = Integer.parseInt(timerMap.get("dakika").toString()) * 60;
                            } else if (timerMap.containsKey("saat")) {
                                seconds = Integer.parseInt(timerMap.get("saat").toString()) * 3600;
                            }

                            String content = timerMap.get("content") != null ? timerMap.get("content").toString() : "";
                            String typeStr = timerMap.get("type") != null ? timerMap.get("type").toString().toUpperCase() : "MESSAGE";

                            Timer timer = new Timer(
                                seconds,
                                TimerType.valueOf(typeStr),
                                ChatColor.translateAlternateColorCodes('&', content)
                            );
                            eventTimers.add(timer);
                        }
                    }
                    timers.put(key, eventTimers);
                } catch (ParseException e) {
                    getLogger().warning("ftpd: " + key);
                } catch (Exception e) {
                    getLogger().warning("err lc " + key + ": " + e.getMessage());
                }
            }
        }
    }

    private void startCountdownChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Date now = new Date();
                for (Map.Entry<String, Date> entry : countdowns.entrySet()) {
                    String event = entry.getKey();
                    Date eventDate = entry.getValue();
                    long secondsUntil = (eventDate.getTime() - now.getTime()) / 1000;

                    List<Timer> eventTimers = timers.get(event);
                    if (eventTimers != null) {
                        for (Timer timer : eventTimers) {
                            if (secondsUntil == timer.getSeconds()) {
                                if (timer.getType() == TimerType.MESSAGE) {
                                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', timer.getContent()));
                                } else if (timer.getType() == TimerType.TITLE) {
                                    String coloredContent = ChatColor.translateAlternateColorCodes('&', timer.getContent());
                                    Bukkit.getOnlinePlayers().forEach(player -> 
                                        player.sendTitle(coloredContent, "", 10, 70, 20));
                                }
                            }
                        }
                    }

                    if (secondsUntil < 0) {
                        countdowns.remove(event);
                        timers.remove(event);
                        getConfig().set("countdowns." + event, null);
                        saveConfig();
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L); 
    }

    public Map<String, Date> getCountdowns() {
        return countdowns;
    }

    public List<Timer> getTimers(String countdownName) {
        return timers.getOrDefault(countdownName, new ArrayList<>());
    }

    public void addCountdown(String name, String displayName, String dateStr) throws ParseException {
        Date date = dateFormat.parse(dateStr);
        countdowns.put(name, date);
        timers.put(name, new ArrayList<>());
        
        getConfig().set("countdowns." + name + ".display_name", displayName);
        getConfig().set("countdowns." + name + ".date", dateFormat.format(date));
        saveConfig();
    }

    public String getDisplayName(String eventId) {
        return getConfig().getString("countdowns." + eventId + ".display_name", eventId);
    }

    public void addTimer(String countdownName, int seconds, TimerType type, String content) {
        List<Timer> eventTimers = timers.getOrDefault(countdownName, new ArrayList<>());
        Timer timer = new Timer(seconds, type, ChatColor.translateAlternateColorCodes('&', content));
        eventTimers.add(timer);
        timers.put(countdownName, eventTimers);

        List<Map<String, Object>> timersList = new ArrayList<>();
        for (Timer t : eventTimers) {
            Map<String, Object> timerMap = new HashMap<>();
            if (t.getSeconds() % 3600 == 0) {
                timerMap.put("saat", t.getSeconds() / 3600);
            } else if (t.getSeconds() % 60 == 0) {
                timerMap.put("dakika", t.getSeconds() / 60);
            } else {
                timerMap.put("saniye", t.getSeconds());
            }
            timerMap.put("type", t.getType().toString());
            timerMap.put("content", t.getContent());
            timersList.add(timerMap);
        }
        
        getConfig().set("countdowns." + countdownName + ".timers", timersList);
        saveConfig();
    }

    public void removeTimer(String countdownName, int index) {
        List<Timer> eventTimers = new ArrayList<>(timers.getOrDefault(countdownName, new ArrayList<>()));
        if (index < eventTimers.size()) {
            eventTimers.remove(index);
            timers.put(countdownName, eventTimers);
            
            if (eventTimers.isEmpty()) {
                getConfig().set("countdowns." + countdownName + ".timers", null);
            } else {
                List<Map<String, Object>> timersList = new ArrayList<>();
                for (Timer t : eventTimers) {
                    Map<String, Object> timerMap = new HashMap<>();
                    if (t.getSeconds() % 3600 == 0) {
                        timerMap.put("saat", t.getSeconds() / 3600);
                    } else if (t.getSeconds() % 60 == 0) {
                        timerMap.put("dakika", t.getSeconds() / 60);
                    } else {
                        timerMap.put("saniye", t.getSeconds());
                    }
                    timerMap.put("type", t.getType().toString());
                    timerMap.put("content", t.getContent());
                    timersList.add(timerMap);
                }
                getConfig().set("countdowns." + countdownName + ".timers", timersList);
            }
            saveConfig();
        }
    }

    public void removeCountdown(String name) {
        countdowns.remove(name);
        timers.remove(name);
        getConfig().set("countdowns." + name, null);
        saveConfig();
        reloadConfig();
    }

    public TimerCreationManager getTimerCreationManager() {
        return timerCreationManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public void refreshCountdowns() {
        countdowns.clear();
        timers.clear();
        reloadConfig();
        loadCountdowns();
    }
}