package me.termalquake;

import me.termalquake.commands.SignPlayedCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayedTimeSign extends JavaPlugin {

    @Override
    public void onEnable() {
        SignPlayedCommand command = new SignPlayedCommand(this);
        getCommand("signplayed").setExecutor(command);
        getCommand("signplayed").setTabCompleter(command);
        getCommand("signreload").setExecutor(new SignReloadCommand(this));
        getLogger().info("Plugin PlayedTimeSign ON!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin PlayedTimeSign OFF!");
    }
}
