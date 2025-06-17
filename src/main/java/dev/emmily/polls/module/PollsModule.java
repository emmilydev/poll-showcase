package dev.emmily.polls.module;

import dev.emmily.polls.PollsPlugin;
import dev.emmily.polls.config.Configuration;
import dev.emmily.polls.message.Messages;
import dev.emmily.polls.poll.Poll;
import dev.emmily.polls.util.SoundHelper;
import dev.emmily.polls.util.time.TimeFormatter;
import me.yushust.inject.AbstractModule;

import dev.emmily.polls.module.model.ModelModule;
import me.yushust.inject.Provides;
import me.yushust.message.MessageHandler;
import me.yushust.message.bukkit.BukkitMessageAdapt;
import me.yushust.message.bukkit.SpigotLinguist;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PollsModule
  extends AbstractModule {
  private final PollsPlugin plugin;

  public PollsModule(PollsPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  protected void configure() {
    bind(PollsPlugin.class).toInstance(plugin);

    Configuration config = new Configuration(
      plugin, "config.yml"
    );
    bind(Configuration.class).toInstance(config);

    install(new ModelModule(config));
  }

  @Provides
  public MessageHandler provideMessageHandler(SoundHelper soundHelper) {
    return MessageHandler.of(
      BukkitMessageAdapt.newYamlSource(plugin),
      config -> {
        config
          .specify(Player.class)
          .resolveFrom(UUID.class, Bukkit::getPlayer)
          .setLinguist(new SpigotLinguist())
          .setMessageSender((player, mode, message) -> {
            soundHelper.play(player, mode);
            player.sendMessage(Messages.from(message));
          });
      }
    );
  }
}
