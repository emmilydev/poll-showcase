package dev.emmily.polls;

import dev.emmily.polls.poll.Poll;
import dev.emmily.sigma.api.repository.CachedAsyncModelRepository;
import me.yushust.message.MessageHandler;
import org.bukkit.plugin.java.JavaPlugin;

public class PollsPlugin
  extends JavaPlugin {
  private MessageHandler messageHandler;
  private CachedAsyncModelRepository<Poll> pollRepository;


  @Override
  public void onEnable() {

  }

  @Override
  public void onDisable() {

  }

  public MessageHandler messageHandler() {
    return messageHandler;
  }
}
