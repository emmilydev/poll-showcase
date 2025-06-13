package dev.emmily.polls.menu;

import dev.emmily.polls.message.MessageMode;
import dev.emmily.polls.poll.Poll;
import dev.emmily.polls.poll.PollService;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import me.yushust.message.MessageHandler;
import me.yushust.message.util.ReplacePack;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.incendo.interfaces.core.click.ClickHandler;
import org.incendo.interfaces.paper.PlayerViewer;
import org.incendo.interfaces.paper.element.ItemStackElement;
import org.incendo.interfaces.paper.pane.ChestPane;
import org.incendo.interfaces.paper.type.ChestInterface;

import java.util.ArrayList;
import java.util.List;

public class AdminPollMenuNavigator
  extends AbstractChestMenu {
  private final MessageHandler messageHandler;
  private final PollService pollService;

  public AdminPollMenuNavigator(MessageHandler messageHandler,
                                PollService pollService) {
    super("poll-admin", messageHandler);
    this.messageHandler = messageHandler;
    this.pollService = pollService;
  }

  public void openCreateMenu(Player player, Poll poll) {
    ChestInterface menu = ChestInterface
      .builder()
      .title(title(player, ReplacePack.make(
        "%question", poll.question()
      )))
      .rows(3)
      .addTransform(fill())
      .addTransform((pane, view) -> {
        ObjectSet<String> options = poll.options().keySet();

        int index = 0;
        for (String option : options) {
          ItemStackElement<ChestPane> pollElement = ItemStackElement.of(
            item(player, "option", ReplacePack.make("%option%", option))
              .type(Material.PAPER)
              .amount(1)
              .build(),
            ClickHandler.canceling(context ->
              openOptionEditMenu(player, poll, option))
          );

          pane.element(pollElement, index++ + 10, 1);
        }

        return pane;
      })
      .addCloseHandler((event, view) -> {
        if (poll.options().isEmpty()) {
          messageHandler.sendIn(player, MessageMode.ERROR, "poll.empty");

          return;
        }

        pollService.register(poll);
      })
      .build();

    menu.open(PlayerViewer.of(player));
  }

  public void openOptionEditMenu(Player player, Poll poll,
                                 String editingOption) {
    if (editingOption == null) {
      editingOption = messageHandler.get(
        player, "menu.poll-admin.default-value"
      );
    }

    String finalEditingOption = editingOption;

    new AnvilGUI.Builder()
      .title(messageHandler.get(player, "menu.poll-admin.title"))
      .text(editingOption)
      .onClose(snapshot -> {
        String newValue = snapshot.getText();
        Object2IntMap<String> options = poll.options();

        options.removeInt(finalEditingOption);
        options.put(newValue, 0);

        openCreateMenu(player, poll);
      });
  }
}
