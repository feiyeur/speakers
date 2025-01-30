package me.feiyeur.speakers;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

public class CommandKit implements TabExecutor {
  private final SpeakersPlugin plugin;

  public CommandKit(SpeakersPlugin plugin) {
    this.plugin = plugin;
  }

  public static class SpeakerGroup {
    public String name;

    private Sound sound = null;
    public boolean getPlaying() {
      return sound != null;
    }
    public String getSoundName() {
      if (sound == null) return null;
      return sound.name();
    }

    // where are the speakers?
    private final List<Location> speakers = new ArrayList<>();

    public final List<Location> getSpeakers() {
      return speakers;
    }

    public SpeakerGroup(String name) {
      this.name = name;
    }

    public void add(Location location) {
      speakers.add(location);
    }

    public void handlePlayerResume(Player p) {
      if (!getPlaying()) return;
      play(p, sound);
    }

    // play, to all players
    public void playAll(Plugin plugin, Sound sound) {
      // World.playSound will only work when the player is near the location
      // stop trying I tried it for u
      if (getPlaying()) stop();
      this.sound = sound;
      for (Player p : Bukkit.getOnlinePlayers()) {
        Bukkit.getScheduler().runTask(plugin, () -> play(p));
      }
    }

    // play, to a specific player
    public void play(Player p, Sound sound) {
      this.sound = sound;
      for (Location loc : speakers) {
        if (loc.getWorld().getUID() != p.getWorld().getUID()) continue;
        p.playSound(loc, sound, 1.0f, 1.0f);
      }
    }

    // play, to a specific player, using existing sound var
    public void play(Player p) {
      for (Location loc : speakers) {
        if (loc.getWorld().getUID() != p.getWorld().getUID()) continue;
        p.playSound(loc, sound, 1.0f, 1.0f);
      }
    }

    public void stop() {
      if(!getPlaying()) return;
      for (Player p : Bukkit.getOnlinePlayers()) {
        p.stopSound(this.sound);
      }
      this.sound = null;
    }
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String label, String[] args) {
    List<String> suggestion = new ArrayList<>();

    if (args.length == 1) {
      // root
      suggestion.add("create");
      suggestion.add("add");
      suggestion.add("ls");
      suggestion.add("play");
      suggestion.add("stop");
      suggestion.add("delete");
    } else if (args.length == 2) {
      // create (for knowing how you've named groups previously), add, ls, play, stop
      if (args[0].equalsIgnoreCase("play") || args[0].equalsIgnoreCase("stop")) suggestion.add("*");
      for (SpeakerGroup group : plugin.speakerGroups) {
        if (StringUtil.startsWithIgnoreCase(group.name, args[1])) suggestion.add(group.name);
      }
    } else if (args.length == 3 && args[0].equalsIgnoreCase("play")) {
      // play <group> <sound>
      for (Sound sound : Sound.values()) {
        if (StringUtil.startsWithIgnoreCase(sound.name(), args[2])) suggestion.add(sound.name());
      }
    }
    return suggestion;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (args.length == 0) {
      return false;
    }
    switch (args[0].toLowerCase()) {
      case "create":
        if (args.length > 1) {
          createSpeakerGroup(sender, args[1]);
        } else {
          sender.sendMessage("Specify a speaker group");
        }
        break;
      case "add":
        if (args.length > 1) {
          addSpeaker(sender, args[1]);
        } else {
          sender.sendMessage("Specify a speaker group");
        }
        break;
      case "ls":
        if (args.length > 1) {
          listSpeakers(sender, args[1]);
        } else {
          listGroups(sender);
        }
        break;
      case "play":
        if (args.length > 2) {
          try {
            playGroup(sender, args[1], Sound.valueOf(args[2]));
          } catch (Exception e) {
            sender.sendMessage(e.getMessage());
          }
        } else {
          sender.sendMessage("Parameters missing!!!!!\n/speakers play <group> <sound>");
        }
        break;
      case "stop":
        if (args.length > 1) {
          stopGroup(sender, args[1]);
        } else {
          sender.sendMessage("Specify a group to stop");
        }
        break;
      case "delete":
        if (args.length > 1) {
          deleteGroup(sender, args[1]);
        } else {
          sender.sendMessage("Specify the group to be deleted");
        }
        break;
      default:
        return false;
    }
    return true;
  }

  private void createSpeakerGroup(CommandSender sender, String name) {
    for (SpeakerGroup group : plugin.speakerGroups) {
      if (group.name.equals(name)) {
        sender.sendMessage("Group w this name already exists");
        return;
      }
    }
    plugin.speakerGroups.add(new SpeakerGroup(name));
    plugin.config.saveConfig();
    sender.sendMessage("Group '"+name+"' created");
  }

  private void addSpeaker(CommandSender sender, String name) {
    if (!(sender instanceof Player p)) {
      sender.sendMessage("You must be a player");
      return;
    }

    SpeakerGroup group = findGroupByName(name);
    if (group == null) {
      sender.sendMessage("Group not found");
      return;
    }

    group.add(p.getLocation());
    if (group.getPlaying()) {
      group.stop();
      group.playAll(plugin, group.sound);
    }
    plugin.config.saveConfig();
    sender.sendMessage("Added a speaker in your location");
  }

  private void listGroups(CommandSender sender) {
    StringBuilder msg = new StringBuilder("Speaker groups:");

    for (SpeakerGroup group : plugin.speakerGroups) {
      msg.append("\n- ").append(group.name).append(" [").append(group.speakers.size()).append("]");
    }

    sender.sendMessage(msg.toString());
  }

  private void listSpeakers(CommandSender sender, String name) {
    SpeakerGroup g = findGroupByName(name);
    if (g == null) {
      sender.sendMessage("Group not found");
      return;
    }

    StringBuilder msg = new StringBuilder("Speaker locations of the group '"+g.name+"':");
    for (Location speakers : g.speakers) {
      msg.append("\n- [")
          .append(Objects.requireNonNull(speakers.getWorld()).getName())
          .append("]")
          .append(speakers.getX())
          .append(", ")
          .append(speakers.getY())
          .append(", ")
          .append(speakers.getZ());
    }

    sender.sendMessage(msg.toString());
  }

  private void playGroup(CommandSender sender, String name, Sound sound) {
    if (name.equalsIgnoreCase("*")) {
      sender.sendMessage("Playing every group w sound '" + sound.name()+"'");
      for (SpeakerGroup g : plugin.speakerGroups) {
        g.playAll(plugin, sound);
      }
      plugin.config.saveConfig();
      return;
    }

    SpeakerGroup g = findGroupByName(name);
    if (g == null) {
      sender.sendMessage("Group not found");
      return;
    }

    sender.sendMessage("Playing " + g.name);
    g.playAll(plugin, sound);
    plugin.config.saveConfig();
  }

  private void stopGroup(CommandSender sender, String name) {
    if (name.equalsIgnoreCase("*")) {
      sender.sendMessage("Stopping every group");
      for (SpeakerGroup g : plugin.speakerGroups) {
        g.stop();
      }
      plugin.config.saveConfig();
      return;
    }

    SpeakerGroup g = findGroupByName(name);
    if (g == null) {
      sender.sendMessage("Group not found");
      return;
    }

    sender.sendMessage("Stopping " + g.name);
    g.stop();
    plugin.config.saveConfig();
  }

  private void deleteGroup(CommandSender sender, String name) {
    SpeakerGroup g = findGroupByName(name);
    if (g == null) {
      sender.sendMessage("Group not found");
      return;
    }

    g.stop();
    plugin.speakerGroups.remove(g);
    plugin.config.saveConfig();
    sender.sendMessage("Group '"+name+"' deleted");
  }

  private SpeakerGroup findGroupByName(String groupName) {
    for (SpeakerGroup g : plugin.speakerGroups) {
      if (g.name.equals(groupName)) {
        return g;
      }
    }
    return null;
  }
}
