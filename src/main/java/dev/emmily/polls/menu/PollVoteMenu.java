package dev.emmily.polls.menu;

import dev.emmily.polls.message.Messages;
import dev.emmily.polls.poll.Poll;
import dev.emmily.polls.poll.PollService;
import me.yushust.message.MessageHandler;
import me.yushust.message.util.ReplacePack;
import org.bukkit.entity.Player;
import org.incendo.interfaces.core.click.ClickHandler;
import org.incendo.interfaces.paper.element.ItemStackElement;
import org.incendo.interfaces.paper.type.ChestInterface;

import java.util.ArrayList;
import java.util.List;

public class PollVoteMenu
  extends AbstractChestMenu {
  private final MessageHandler messageHandler;
  private final PollService pollService;

  public PollVoteMenu(MessageHandler messageHandler,
                      PollService pollService) {
    super("poll-vote", messageHandler);
    this.messageHandler = messageHandler;
    this.pollService = pollService;
  }

  public void open(Player player, Poll poll) {
    ChestInterface menu = ChestInterface
      .builder()
      .title(Messages.from(messageHandler.replacing(
        player, "menu.poll-vote.title"
      )))
      .clickHandler(ClickHandler.cancel())
      .rows(6)
      .addTransform(fill())
      .addTransform((pane, view) -> {
        List<String> options = new ArrayList<>(poll.options().keySet());

        for (int i = 10; i < 16; i++) {
          String option = options.get(i);

          pane.element(ItemStackElement.of(item(
            player, "option",
            ReplacePack.make("%option%", option)
          ).build(), ClickHandler.canceling(context ->
            pollService.vote(poll, option, player))), i, 1);
        }

        return pane;
      })
      .build();
  }
}
