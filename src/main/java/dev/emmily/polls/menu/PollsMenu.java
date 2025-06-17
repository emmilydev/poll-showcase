package dev.emmily.polls.menu;

import dev.emmily.polls.poll.PollService;
import dev.emmily.polls.util.time.TimeFormatter;
import me.yushust.message.MessageHandler;
import me.yushust.message.util.ReplacePack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.interfaces.core.arguments.ArgumentKey;
import org.incendo.interfaces.core.click.ClickContext;
import org.incendo.interfaces.core.click.ClickHandler;
import org.incendo.interfaces.core.transform.types.PaginatedTransform;
import org.incendo.interfaces.core.util.Vector2;
import org.incendo.interfaces.paper.PlayerViewer;
import org.incendo.interfaces.paper.element.ItemStackElement;
import org.incendo.interfaces.paper.pane.ChestPane;
import org.incendo.interfaces.paper.type.ChestInterface;

import javax.inject.Inject;
import java.util.List;

public class PollsMenu extends AbstractChestMenu {
  private final PollService pollService;
  private final PollVoteMenu pollVoteMenu;
  private final TimeFormatter timeFormatter;

  @Inject
  public PollsMenu(
    MessageHandler messageHandler,
    PollService pollService,
    PollVoteMenu pollVoteMenu,
    TimeFormatter timeFormatter
  ) {
    super("polls", messageHandler);
    this.pollService = pollService;
    this.pollVoteMenu = pollVoteMenu;
    this.timeFormatter = timeFormatter;
  }

  public void open(Player player) {
    List<ItemStackElement<ChestPane>> pollElements = pollService
      .getAllPolls()
      .stream()
      .map(poll -> ItemStackElement.<ChestPane>of(
        item(
          player, "poll",
          ReplacePack.EMPTY, poll
        ).type(Material.PAPER)
          .amount(1)
          .build(),
        context -> {
          context.status(ClickContext.ClickStatus.DENY);
          pollVoteMenu.open(player, poll);
        }
      ))
      .toList();

    PaginatedTransform<ItemStackElement<ChestPane>, ChestPane, PlayerViewer> transform =
      new PaginatedTransform<>(
        Vector2.at(1, 1),
        Vector2.at(4, 7),
        () -> pollElements
      );

    transform.backwardElement(
      Vector2.at(5, 0),
      $ -> ItemStackElement.of(
        createNavigator(player, false),
        ClickHandler.cancel()
      )
    );

    transform.forwardElement(
      Vector2.at(5, 8),
      $ -> ItemStackElement.of(
        createNavigator(player, true),
        ClickHandler.cancel()
      )
    );

    ChestInterface menu = ChestInterface
      .builder()
      .title(title(player, ReplacePack.make(
        "%page%", transform.pageProperty().get()
      )))
      .clickHandler(ClickHandler.cancel())
      .rows(6)
      .addTransform(transform)
      .build();

    menu.open(PlayerViewer.of(player));
  }

  private ItemStack createNavigator(Player player, boolean forward) {
    String element = (forward ? "next" : "previous") + "page";
    return item(player, element)
      .type(Material.ARROW)
      .amount(1)
      .build();
  }
}
