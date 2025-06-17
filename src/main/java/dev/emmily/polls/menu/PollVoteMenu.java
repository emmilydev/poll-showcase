package dev.emmily.polls.menu;

import dev.emmily.polls.message.Messages;
import dev.emmily.polls.poll.Poll;
import dev.emmily.polls.poll.PollService;
import me.yushust.message.MessageHandler;
import me.yushust.message.util.ReplacePack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.incendo.interfaces.core.click.ClickHandler;
import org.incendo.interfaces.core.util.Vector2;
import org.incendo.interfaces.paper.PlayerViewer;
import org.incendo.interfaces.paper.element.ItemStackElement;
import org.incendo.interfaces.paper.pane.ChestPane;
import org.incendo.interfaces.paper.type.ChestInterface;

import javax.inject.Inject;
import java.util.List;

public class PollVoteMenu extends AbstractChestMenu {
  private final MessageHandler messageHandler;
  private final PollService pollService;

  @Inject
  public PollVoteMenu(
    MessageHandler messageHandler,
    PollService pollService
  ) {
    super("poll-vote", messageHandler);
    this.messageHandler = messageHandler;
    this.pollService = pollService;
  }

  public void open(Player player, Poll poll) {
    List<Poll.Option> options = poll.options().values()
      .stream()
      .filter(option -> option.value() != null)
      .toList();

    ChestInterface menu = ChestInterface.builder()
      .title(Messages.from(messageHandler.get(
        player, "menu.poll-vote.title",
        poll
      )))
      .clickHandler(ClickHandler.cancel())
      .rows(6)
      .addTransform(fill())
      .addTransform((pane, view) -> {
        int index = 0;

        for (Vector2 coordinate : centeredCoordinates(options.size())) {
          Poll.Option option = options.get(index++);

          ItemStackElement<ChestPane> element = ItemStackElement.of(
            item(
              player, "option",
              ReplacePack.make("%option%", option),
              poll
            )
              .type(Material.PAPER)
              .amount(1)
              .build(),
            ClickHandler.canceling(context -> {
              pollService.vote(poll, option.index(), player);
              player.closeInventory();
            })
          );

          pane = pane.element(element, coordinate.x(), coordinate.y());
        }

        return pane;
      })
      .build();

    menu.open(PlayerViewer.of(player));
  }
}
