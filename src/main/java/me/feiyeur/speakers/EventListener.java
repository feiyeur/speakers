package me.feiyeur.speakers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventListener implements Listener {
  private final SpeakersPlugin plugin;

  public EventListener(SpeakersPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent e) {
    for (CommandKit.SpeakerGroup group : plugin.speakerGroups) {
      group.handlePlayerResume(e.getPlayer());
    }
  }

  @EventHandler
  public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {
    for (CommandKit.SpeakerGroup group : plugin.speakerGroups) {
      group.handlePlayerResume(e.getPlayer());
    }
  }
}
