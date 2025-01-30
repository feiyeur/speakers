package me.feiyeur.speakers;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpeakersPlugin extends JavaPlugin {
  private final PluginManager pm = Bukkit.getPluginManager();
  public final ConfigManager config = new ConfigManager(this);
  public List<CommandKit.SpeakerGroup> speakerGroups = new ArrayList<>();

  @Override
  public void onEnable() {
    config.loadConfig();

    pm.registerEvents(new EventListener(this), this);
    this.getCommand("speakers").setExecutor(new CommandKit(this));
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
  }
}
