package me.termalquake;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class SignReloadCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public SignReloadCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("signplayed.reload")) {
            sender.sendMessage("§cУ вас нет прав на выполнение этой команды.");
            return true;
        }

        plugin.reloadConfig();
        sender.sendMessage("§aПлагин перезагружен.");
        return true;
    }
}
