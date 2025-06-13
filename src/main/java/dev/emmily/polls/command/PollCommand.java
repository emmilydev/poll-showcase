package dev.emmily.polls.command;

import dev.emmily.polls.menu.AdminPollMenuNavigator;
import dev.emmily.polls.menu.PollsMenu;
import dev.emmily.polls.poll.Poll;
import dev.emmily.polls.poll.PollService;
import me.fixeddev.commandflow.annotated.CommandClass;
import me.fixeddev.commandflow.annotated.annotation.Command;
import me.fixeddev.commandflow.annotated.annotation.Text;
import me.fixeddev.commandflow.bukkit.annotation.Sender;
import org.bukkit.entity.Player;

@Command(
  names = "poll"
)
public class PollCommand
  implements CommandClass {
  private final PollsMenu pollsMenu;
  private final AdminPollMenuNavigator adminPollMenuNavigator;
  private final PollService pollService;

  public PollCommand(PollsMenu pollsMenu,
                     AdminPollMenuNavigator adminPollMenuNavigator,
                     PollService pollService) {
    this.pollsMenu = pollsMenu;
    this.adminPollMenuNavigator = adminPollMenuNavigator;
    this.pollService = pollService;
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
    Poll poll = Poll.of(
      id, player.getUniqueId().toString(),
      question, Long.parseLong(expireDate) // TODO: don't! use a parser
    );
    adminPollMenuNavigator.openCreateMenu(player, poll);
  }

  @Command(
    names = "close",
    permission = "polls.close"
  )
  public void runCloseCommand(@Sender Player player,
                              Poll poll) {
    poll.setClosed(true);
    pollService.expire(poll);
  }
}
