package ru.governix.prefix;

import org.bukkit.plugin.java.JavaPlugin;
import ru.governix.prefix.commands.PrefixCommand;

public class GovernixPrefix extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        PrefixCommand cmd = new PrefixCommand(this);
        getCommand("cprefix").setExecutor(cmd);
        getCommand("cprefix").setTabCompleter(cmd);

        getLogger().info("GovernixPrefix v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("GovernixPrefix disabled!");
    }

    public String msg(String key) {
        return getConfig().getString("messages." + key, "&cMissing: " + key);
    }
}
