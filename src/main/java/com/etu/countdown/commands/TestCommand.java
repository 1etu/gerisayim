package com.etu.countdown.commands;

import com.etu.countdown.BaseClass;
import com.etu.countdown.TimerType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// sil
public class TestCommand implements CommandExecutor {
    private final BaseClass plugin;

    public TestCommand(BaseClass plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, 1);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm", new Locale("tr"));
        String dateStr = dateFormat.format(cal.getTime());
        String testEventId = "test_" + System.currentTimeMillis();
        String displayName = "Test Etkinliği";

        try {
            plugin.addCountdown(testEventId, displayName, dateStr);
            
            plugin.addTimer(testEventId, 55, TimerType.MESSAGE, "&a&lTest etkinliğine 55 saniye kaldı!");
            plugin.addTimer(testEventId, 45, TimerType.MESSAGE, "&e&lTest etkinliğine 45 saniye kaldı!");
            plugin.addTimer(testEventId, 30, TimerType.MESSAGE, "&e&lTest etkinliğine 30 saniye kaldı!");
            plugin.addTimer(testEventId, 15, TimerType.TITLE, "&6&l15 SANİYE KALDI!");
            plugin.addTimer(testEventId, 10, TimerType.TITLE, "&c&l10 SANİYE KALDI!");
            plugin.addTimer(testEventId, 5, TimerType.MESSAGE, "&6&lTest etkinliğine 5 saniye kaldı!");
            plugin.addTimer(testEventId, 0, TimerType.TITLE, "&4&lETKİNLİK BAŞLADI!");

            sender.sendMessage(ChatColor.GREEN + "Test etkinliği oluşturuldu! " + dateStr + " tarihinde başlayacak.");
            sender.sendMessage(ChatColor.YELLOW + "Tüm zamanlayıcılar: 55s, 45s, 30s, 15s, 10s, 5s ve başlangıç");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Test etkinliği oluşturulurken bir hata oluştu: " + e.getMessage());
        }

        return true;
    }
} 