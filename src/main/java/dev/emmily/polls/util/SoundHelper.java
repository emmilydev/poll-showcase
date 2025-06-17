package dev.emmily.polls.util;

import dev.emmily.polls.config.Configuration;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.NoSuchElementException;

@Singleton
public class SoundHelper {
  private static Sound parse(String source) {
    if (source == null) {
      throw new NoSuchElementException("Couldn't find the specified sound");
    }

    String[] parts = source.split(" ");

    return Sound.sound(
      Key.key(parts[0]),
      Sound.Source.MASTER,
      Float.parseFloat(parts[1]),
      Float.parseFloat(parts[2])
    );
  }

  private final Sound click;
  private final Sound info;
  private final Sound warning;
  private final Sound error;

  @Inject
  public SoundHelper(Configuration configuration) {
    this.click = parse(configuration.getString("sound.click"));
    this.info = parse(configuration.getString("sound.info"));
    this.warning = parse(configuration.getString("sound.warning"));
    this.error = parse(configuration.getString("sound.error"));
  }

  public void playClick(Player player) {
    player.playSound(click);
  }

  public void playInfo(Player player) {
    player.playSound(info);
  }

  public void playWarning(Player player) {
    player.playSound(warning);
  }

  public void playError(Player player) {
    player.playSound(error);
  }

  public void play(Player player, String mode) {
    Sound sound = switch (mode) {
      case "click" -> click;
      case "info" -> info;
      case "warning" -> warning;
      case "error" -> error;
      case null, default -> null;
    };

    if (sound == null) {
      return;
    }

    player.playSound(sound);
  }
}
