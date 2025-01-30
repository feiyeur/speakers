package me.feiyeur.speakers;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
  private final SpeakersPlugin plugin;

  public ConfigManager(SpeakersPlugin plugin) {
    this.plugin = plugin;
  }

  public void loadConfig() {
    plugin.getLogger().info("Loading existing speaker groups...");
    plugin.saveDefaultConfig();

    plugin.speakerGroups = new ArrayList<>();

    ConfigurationSection groupSection = plugin.getConfig().getConfigurationSection("groups");
    if (groupSection != null) {
      // groups.a
      for (String groupName : groupSection.getKeys(false)) {
        ConfigurationSection locSection = groupSection.getConfigurationSection(groupName);
        if (locSection == null) continue;

        CommandKit.SpeakerGroup g = new CommandKit.SpeakerGroup(groupName);

        String sound = locSection.getString("sound", null);

        // groups.a.speakers.0
        ConfigurationSection speakerSection = locSection.getConfigurationSection("speakers");
        if (speakerSection == null) continue;

        for (String locKey : speakerSection.getKeys(false)) {
          double x = speakerSection.getDouble(locKey + ".x");
          double y = speakerSection.getDouble(locKey + ".y");
          double z = speakerSection.getDouble(locKey + ".z");
          String worldName = speakerSection.getString(locKey + ".world");

          g.add(new Location(Bukkit.getWorld(worldName), x, y, z));
        }

        plugin.speakerGroups.add(g);
        if (sound != null) g.playAll(plugin, Sound.valueOf(sound));

        plugin
            .getLogger()
            .info(
                "Loaded group '"
                    + groupName
                    + "' ("
                    + g.getSpeakers().size()
                    + ")"
                    + (g.getPlaying() ? " [" + g.getSoundName() + "]" : ""));
      }
    }
    plugin.getLogger().info("All groups loaded");
  }

  public void saveConfig() {
    plugin.getLogger().info("Saving configuration...");
    FileConfiguration config = plugin.getConfig();
    config.set("groups", null);

    for (CommandKit.SpeakerGroup group : plugin.speakerGroups) {
      ConfigurationSection groupSection = config.createSection("groups." + group.name);
      groupSection.set("sound", group.getSoundName());

      // groups.a.speakers
      ConfigurationSection speakersSection = groupSection.createSection("speakers");
      List<Location> speakers = group.getSpeakers();
      for (int i = 0; i < speakers.size(); i++) {
        Location loc = speakers.get(i);
        ConfigurationSection locSection = speakersSection.createSection(String.valueOf(i));
        locSection.set("world", loc.getWorld().getName());
        locSection.set("x", loc.getX());
        locSection.set("y", loc.getY());
        locSection.set("z", loc.getZ());
      }
    }

    plugin.saveConfig();
    plugin.getLogger().info("Configuration saved");
  }
}
