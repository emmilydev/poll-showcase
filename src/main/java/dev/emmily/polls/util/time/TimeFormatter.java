package dev.emmily.polls.util.time;

import me.yushust.message.MessageHandler;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalLong;

// stolen and adapted from MineAqua/MAPractice
@Singleton
public final class TimeFormatter {

  private static final Map<Character, ChronoUnit> DIGITS = new HashMap<>();
  private static final Map<String, ChronoUnit> UNITS = new LinkedHashMap<>();

  private static final String HOUR_FORMAT = "%02d:%02d:%02d";
  private static final String MINUTE_FORMAT = "%02d:%02d";

  static {
    UNITS.put("year", ChronoUnit.YEARS);
    DIGITS.put('y', ChronoUnit.YEARS);
    UNITS.put("month", ChronoUnit.MONTHS);
    DIGITS.put('M', ChronoUnit.MONTHS);
    UNITS.put("week", ChronoUnit.WEEKS);
    DIGITS.put('w', ChronoUnit.WEEKS);
    UNITS.put("day", ChronoUnit.DAYS);
    DIGITS.put('d', ChronoUnit.DAYS);
    UNITS.put("hour", ChronoUnit.HOURS);
    DIGITS.put('h', ChronoUnit.HOURS);
    UNITS.put("minute", ChronoUnit.MINUTES);
    DIGITS.put('m', ChronoUnit.MINUTES);
    UNITS.put("second", ChronoUnit.SECONDS);
    DIGITS.put('s', ChronoUnit.SECONDS);
  }

  private final MessageHandler messageHandler;

  @Inject
  public TimeFormatter(MessageHandler messageHandler) {
    this.messageHandler = messageHandler;
  }

  public String formatNoWords(long millis) {
    long seconds = millis / 1000L;
    if (seconds > 3600L) {
      return String.format(HOUR_FORMAT, seconds / 3600L, seconds % 3600L / 60L, seconds % 60L);
    } else {
      return String.format(MINUTE_FORMAT, seconds / 60L, seconds % 60L);
    }
  }

  public OptionalLong parse(String formattedTime) {
    long parsed = 0;
    long parsing = 0;
    for (char c : formattedTime.toCharArray()) {
      if (Character.isDigit(c)) {
        parsing *= 10;
        parsing += Character.getNumericValue(c);
        continue;
      }
      if (c == ' ') {
        continue;
      }
      ChronoUnit unit = DIGITS.get(c);
      if (unit == null) {
        return OptionalLong.empty();
      }
      parsing = unit.getDuration().multipliedBy(parsing).toMillis();
      parsed += parsing;
      parsing = 0;
    }
    return OptionalLong.of(parsed);
  }

  public String format(long millis) {
    return format(null, millis, true);
  }

  public String format(long millis, boolean complete) {
    return format(null, millis, complete);
  }

  public String format(@Nullable Object receiver, long millis) {
    return format(receiver, millis, true);
  }

  public String format(@Nullable Object receiver, long millis, boolean complete) {

    if (millis < 1000) {
      long secondTenths = millis / 10;
      return "0." + secondTenths + " " + messageHandler.get(receiver, "word.second.plural");
    }

    StringBuilder timeBuilder = new StringBuilder();

    for (String unitName : UNITS.keySet()) {
      ChronoUnit unit = UNITS.get(unitName);
      long unitTime = millis / unit.getDuration().toMillis();
      millis -= unit.getDuration().multipliedBy(unitTime).toMillis();
      if (unitTime > 0) {
        timeBuilder.append(unitTime);
        String basePath = "word." + unitName + ".";
        if (complete) {
          timeBuilder.append(
            messageHandler.get(receiver, basePath + (unitTime == 1 ? "single" : "plural"))
          );
        } else {
          timeBuilder.append(
            messageHandler.get(receiver, basePath + "short")
          );
        }
      }
    }

    return timeBuilder.toString().trim();
  }

}
