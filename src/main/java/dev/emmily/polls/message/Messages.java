package dev.emmily.polls.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Utility class to convert plain strings to kyori
 * {@link Component components.}
 */
public interface Messages {
  MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

  static Component from(String plain) {
    return MINI_MESSAGE.deserialize(plain);
  }

  static List<Component> fromList(List<String> plain) {
    return plain
      .stream()
      .map(Messages::from)
      .toList();
  }

  static List<String> replace(List<String> list, String pattern,
                              Supplier<List<String>> replacement) {
    List<String> result = new ArrayList<>();
    for (String line : list) {
      int index = line.indexOf(pattern);

      if (index == -1) {
        // pattern not found, just add
        // this line to the result
        result.add(line);
      } else {
        // pattern found!
        String prefix = line.substring(0, index);
        String suffix = line.substring(index + pattern.length());
        List<String> addition = replacement.get();

        if (addition.isEmpty()) {
          // no added lines
          result.add(prefix + suffix);
        } else {
          Iterator<String> iterator = addition.iterator();

          // prepend prefix to first line
          if (iterator.hasNext()) {
            result.add(prefix + iterator.next());
          }

          while (iterator.hasNext()) {
            String current = iterator.next();
            if (iterator.hasNext()) {
              result.add(current);
            } else {
              // append suffix to last line
              result.add(current + suffix);
            }
          }
        }
      }
    }
    return result;
  }
}
