package dev.emmily.polls.menu;

import dev.emmily.polls.item.ItemBuilder;
import dev.emmily.polls.message.Messages;
import me.yushust.message.MessageHandler;
import me.yushust.message.util.ReplacePack;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.incendo.interfaces.core.transform.Transform;
import org.incendo.interfaces.core.util.Vector2;
import org.incendo.interfaces.paper.PlayerViewer;
import org.incendo.interfaces.paper.element.ItemStackElement;
import org.incendo.interfaces.paper.pane.ChestPane;
import org.incendo.interfaces.paper.transform.PaperTransform;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractChestMenu {
  private static final String PATH = "menu.%s";
  private static final String TITLE = PATH + ".title";
  private static final String ITEM = PATH + ".item.%s";
  private static final String ITEM_NAME = ITEM + ".name";
  private static final String ITEM_LORE = ITEM + ".lore";
  private static final ItemStack FILLER =
    new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
  private static final Transform<ChestPane, PlayerViewer> FILLER_TRANSFORM =
    PaperTransform.chestFill(ItemStackElement.of(FILLER));

  private final String id;
  private final MessageHandler messageHandler;

  public AbstractChestMenu(String id, MessageHandler messageHandler) {
    this.id = id;
    this.messageHandler = messageHandler;
  }

  protected Transform<ChestPane, PlayerViewer> fill() {
    return FILLER_TRANSFORM;
  }

  protected Component title(
    Player player,
    ReplacePack replacePack,
    Object... jitEntities
  ) {
    return Messages.from(messageHandler.format(
      player,
      String.format(TITLE, id),
      replacePack,
      jitEntities
    ));
  }

  protected ItemBuilder item(
    Player player,
    String item,
    ReplacePack replacements,
    Object... jitEntities
  ) {
    return ItemBuilder.builder()
      .name(Messages.from(messageHandler.format(
        player,
        String.format(ITEM_NAME, id, item),
        replacements,
        jitEntities
      )))
      .lore(Messages.fromList(messageHandler.formatMany(
        player,
        String.format(ITEM_LORE, id, item),
        replacements,
        jitEntities
      )));
  }

  protected ItemBuilder item(
    Player player,
    String item,
    Object... replacements
  ) {
    return ItemBuilder.builder()
      .name(Messages.from(messageHandler.replacing(
        player,
        String.format(ITEM_NAME, id, item),
        replacements
      )))
      .lore(Messages.fromList(messageHandler.replacingMany(
        player,
        String.format(ITEM_LORE, id, item),
        replacements
      )))
      .addFlags(ItemFlag.values());
  }

  protected List<Vector2> centeredCoordinates(int count) {
    if (count < 1 || count > 6) {
      throw new IllegalArgumentException("Count must be between 1 and 6");
    }

    List<Vector2> positions = new ArrayList<>();

    int firstRowCount = Math.min(count, 5);
    int secondRowCount = count - firstRowCount;

    int centerX = 4;
    int startX = centerX - (firstRowCount - 1) / 2;

    for (int i = 0; i < firstRowCount; i++) {
      positions.add(Vector2.at(startX + i, 1));
    }

    if (secondRowCount > 0) {
      positions.add(Vector2.at(4, 2));
    }

    return positions;
  }
}
