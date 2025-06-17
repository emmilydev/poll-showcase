package dev.emmily.polls.item;


import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemBuilder {
  private Material type;
  private int amount = 1;
  private Component name;
  private List<Component> lore;
  private List<ItemFlag> flags;
  private Map<Enchantment, Integer> enchantments;

  public static ItemBuilder builder() {
    return new ItemBuilder();
  }

  public ItemBuilder type(Material type) {
    this.type = type;

    return this;
  }

  public ItemBuilder amount(int amount) {
    this.amount = amount;

    return this;
  }

  public ItemBuilder name(Component name) {
    this.name = name;

    return this;
  }

  public ItemBuilder lore(List<Component> lore) {
    this.lore = lore;

    return this;
  }

  public ItemBuilder addLore(Component line) {
    if (lore == null) {
      lore = new ArrayList<>();
    }

    lore.add(line);

    return this;
  }

  public ItemBuilder addFlag(ItemFlag flag) {
    if (flags == null) {
      flags = new ArrayList<>();
    }

    flags.add(flag);

    return this;
  }

  public ItemBuilder addFlags(ItemFlag... flags) {
    if (this.flags == null) {
      this.flags = new ArrayList<>();
    }

    this.flags.addAll(List.of(flags));

    return this;
  }

  public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
    if (enchantments == null) {
      enchantments = new HashMap<>();
    }

    enchantments.put(enchantment, level);

    return this;
  }

  public ItemStack build() {
    ItemStack item = new ItemStack(type, amount);
    ItemMeta meta = item.getItemMeta();

    if (name != null) {
      meta.displayName(name);
    }

    if (lore != null) {
      meta.lore(lore);
    }

    if (flags != null) {
      flags.forEach(meta::addItemFlags);
    }

    if (enchantments != null) {
      enchantments.forEach((enchant, level)
        -> meta.addEnchant(enchant, level, true));
    }

    item.setItemMeta(meta);

    return item;
  }
}
