package dev.emmily.polls.command;

import dev.emmily.polls.menu.AdminPollMenuNavigator;
import dev.emmily.polls.menu.PollsMenu;
import dev.emmily.polls.message.MessageMode;
import dev.emmily.polls.poll.Poll;
import dev.emmily.polls.poll.PollService;
import dev.emmily.polls.util.time.TimeFormatter;
import dev.emmily.sigma.api.repository.ModelRepository;
import me.fixeddev.commandflow.annotated.CommandClass;
import me.fixeddev.commandflow.annotated.annotation.Command;
import me.fixeddev.commandflow.annotated.annotation.Text;
import me.fixeddev.commandflow.bukkit.annotation.Sender;
import me.yushust.message.MessageHandler;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.util.OptionalLong;

@Command(
  names = "poll"
)
public class PollCommand
  implements CommandClass {
  private final PollsMenu pollsMenu;
  private final AdminPollMenuNavigator adminPollMenuNavigator;
  private final PollService pollService;
  private final TimeFormatter timeFormatter;
  private final MessageHandler messageHandler;
  private final ModelRepository<Poll> pollRepository;

  @Inject
  public PollCommand(PollsMenu pollsMenu,
                     AdminPollMenuNavigator adminPollMenuNavigator,
                     PollService pollService,
                     TimeFormatter timeFormatter,
                     MessageHandler messageHandler,
                     ModelRepository<Poll> pollRepository) {
    this.pollsMenu = pollsMenu;
    this.adminPollMenuNavigator = adminPollMenuNavigator;
    this.pollService = pollService;
    this.timeFormatter = timeFormatter;
    this.messageHandler = messageHandler;
    this.pollRepository = pollRepository;
  }

  @Command(
    names = ""
  )
  public void runPollCommand(@Sender Player player) {
    pollsMenu.open(player);
  }

  @Command(
    names = "create",
    permission = "polls.create"
  )
  public void runCreateCommand(@Sender Player player,
                               String id,
                               String expireDate,
                               @Text String question) {
    if (pollRepository.exists(id)) {
      messageHandler.sendReplacingIn(
        player, MessageMode.ERROR,
        "poll.already-exists",
        "%poll%", id
      );

      return;
    }

    OptionalLong time = timeFormatter.parse(expireDate);
    if (time.isEmpty()) {
      messageHandler.sendIn(player, MessageMode.ERROR, "poll.invalid-date");

      return;
    }

    Poll poll = Poll.of(
      id, player.getUniqueId().toString(),
      question, System.currentTimeMillis() + time.getAsLong()
    );

    adminPollMenuNavigator.openCreateMenu(player, poll);

  }

  @Command(
    names = "close",
    permission = "polls.close"
  )
  public void runCloseCommand(@Sender Player player,
                              Poll poll) {
    pollService.expirePoll(poll);
  }
}
