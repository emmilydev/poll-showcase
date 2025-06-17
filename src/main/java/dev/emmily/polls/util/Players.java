package dev.emmily.polls.util;

import org.bukkit.entity.Player;

public interface Players {
  static String getId(Player player) {
    return player.getUniqueId().toString();
  }
}
