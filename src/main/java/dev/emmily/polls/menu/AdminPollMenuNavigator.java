package dev.emmily.polls.menu;

import dev.emmily.polls.PollsPlugin;
import dev.emmily.polls.message.MessageMode;
import dev.emmily.polls.message.Messages;
import dev.emmily.polls.poll.Poll;
import dev.emmily.polls.poll.PollService;
import dev.emmily.polls.util.Strings;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import me.yushust.message.MessageHandler;
import me.yushust.message.util.ReplacePack;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.incendo.interfaces.core.click.ClickHandler;
import org.incendo.interfaces.paper.PlayerViewer;
import org.incendo.interfaces.paper.element.ItemStackElement;
import org.incendo.interfaces.paper.pane.ChestPane;
import org.incendo.interfaces.paper.type.ChestInterface;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Objects;

public class AdminPollMenuNavigator extends AbstractChestMenu {
  private final MessageHandler messageHandler;
  private final PollService pollService;
  private final PollsPlugin plugin;

  @Inject
  public AdminPollMenuNavigator(
    MessageHandler messageHandler,
    PollService pollService,
    PollsPlugin plugin
  ) {
    super("poll-admin", messageHandler);
    this.messageHandler = messageHandler;
    this.pollService = pollService;
    this.plugin = plugin;
  }

  public void openCreateMenu(Player player, Poll poll) {
    ChestInterface menu = ChestInterface.builder()
      .title(title(player, ReplacePack.EMPTY, poll))
      .rows(3)
      .addTransform(fill())
      .addTransform((pane, view) -> {
        pane = pane.element(ItemStackElement.of(
          item(player, "confirm")
            .type(Material.GREEN_WOOL)
            .amount(1)
            .addFlags(ItemFlag.values())
            .build(),
          ClickHandler.canceling(context -> {
            boolean allNull = poll.options().values().stream()
              .map(Poll.Option::value)
              .allMatch(Objects::isNull);

            if (allNull) {
              messageHandler.sendIn(player, MessageMode.ERROR, "poll.empty");

              return;
            }

            pollService.registerPoll(poll);
            messageHandler.send(player, "poll.created", poll);
            player.closeInventory();
          })
        ), 8, 2);

        ObjectCollection<Poll.Option> options = poll.options().values();
        int index = 2;

        String defaultValue = messageHandler.get(player, "menu.poll-admin.default-value");

        for (Poll.Option option : options) {
          ItemStackElement<ChestPane> pollElement = ItemStackElement.of(
            item(
              player,
              "option",
              ReplacePack.make("%option%", Strings.valueOrDef(option.value(), defaultValue)),
              poll
            )
              .type(Material.PAPER)
              .amount(1)
              .build(),
            ClickHandler.canceling(context ->
              openOptionEditMenu(player, poll, option)
            )
          );

          if (index == 7) {
            index = 4;
            return pane.element(pollElement, index, 2);
          }

          pane = pane.element(pollElement, index++, 1);
        }

        return pane;
      })
      .build();

    menu.open(PlayerViewer.of(player));
  }

  public void openOptionEditMenu(Player player, Poll poll, Poll.Option editingOption) {
    String displayedValue = editingOption.value();

    if (displayedValue == null) {
      displayedValue = messageHandler.get(player, "menu.poll-admin.default-value");
    }

    new AnvilGUI.Builder()
      .plugin(plugin)
      .title(Messages.strip(messageHandler.get(player, "menu.poll-admin.title", poll)))
      .text(Messages.strip(displayedValue))
      .onClick((slot, snapshot) -> {
        if (slot != AnvilGUI.Slot.OUTPUT) {
          return Collections.emptyList();
        }
        return Collections.singletonList(AnvilGUI.ResponseAction.close());
      })
      .onClose(snapshot -> {
        String newValue = snapshot.getText();
        editingOption.setValue(newValue);
        openCreateMenu(player, poll);
      })
      .open(player);
  }
}
