package dev.emmily.polls.command.service;

import dev.emmily.polls.command.PollCommand;
import dev.emmily.polls.command.part.PollPart;
import dev.emmily.polls.poll.Poll;
import me.fixeddev.commandflow.CommandManager;
import me.fixeddev.commandflow.SimpleCommandManager;
import me.fixeddev.commandflow.annotated.AnnotatedCommandTreeBuilder;
import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.annotated.part.defaults.DefaultsModule;
import me.fixeddev.commandflow.bukkit.BukkitAuthorizer;
import me.fixeddev.commandflow.bukkit.BukkitCommandManager;
import me.fixeddev.commandflow.bukkit.factory.BukkitModule;

public class CommandService {
  private final PollPart pollPart;
  private final PollCommand pollCommand;

  public CommandService(PollPart pollPart, PollCommand pollCommand) {
    this.pollPart = pollPart;
    this.pollCommand = pollCommand;
  }

  public void registerCommands() {
    PartInjector partInjector = PartInjector.create();
    partInjector.install(new DefaultsModule());
    partInjector.install(new BukkitModule());
    partInjector.bindFactory(Poll.class, pollPart);

    CommandManager manager = new BukkitCommandManager(
      new SimpleCommandManager(new BukkitAuthorizer()),
      "polls"
    );
    AnnotatedCommandTreeBuilder builder = AnnotatedCommandTreeBuilder.create(partInjector);
    manager.registerCommands(builder.fromClass(pollCommand));
  }
}
