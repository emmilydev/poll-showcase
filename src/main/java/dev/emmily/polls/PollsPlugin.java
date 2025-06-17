package dev.emmily.polls;

import dev.emmily.polls.command.service.CommandService;
import dev.emmily.polls.config.Configuration;
import dev.emmily.polls.module.PollsModule;
import dev.emmily.polls.mongo.MongoClientService;
import dev.emmily.polls.poll.PollService;
import me.yushust.inject.Injector;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.interfaces.paper.PaperInterfaceListeners;

import javax.inject.Inject;

public class PollsPlugin
  extends JavaPlugin {
  @Inject private MongoClientService mongoClientService;
  @Inject private CommandService commandService;
  @Inject private Configuration config;
  @Inject private PollService pollService;

  @Override
  public void onEnable() {
    Injector injector = Injector.create(new PollsModule(this));
    injector.injectMembers(this);

    PaperInterfaceListeners.install(this);
    commandService.registerCommands();
    pollService.loadAll();
  }

  @Override
  public void onDisable() {
    mongoClientService.close();
    pollService.saveAll();
  }
}
