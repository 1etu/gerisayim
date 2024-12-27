package com.etu.countdown.commands;

import com.etu.countdown.BaseClass;
import com.etu.countdown.gui.GUIManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Editor implements CommandExecutor {
    @SuppressWarnings("unused")
    private final BaseClass plugin;
    private final GUIManager guiManager;

    public Editor(BaseClass plugin) {
        this.plugin = plugin;
        this.guiManager = new GUIManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komutu sadece oyuncular kullanabilir!");
            return true;
        }

        Player player = (Player) sender;
        player.openInventory(guiManager.createMainMenu());
        return true;
    }
}