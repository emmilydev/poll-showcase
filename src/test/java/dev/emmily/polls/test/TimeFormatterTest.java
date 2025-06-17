package dev.emmily.polls.test;

import dev.emmily.polls.util.time.TimeFormatter;
import me.yushust.message.MessageHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimeFormatterTest {
  public static final Map<String, String> MESSAGE_SOURCE = new HashMap<>(Map.ofEntries(
    Map.entry("word.second.single", "segundo"),
    Map.entry("word.second.plural", "segundos"),
    Map.entry("word.second.short", "s"),
    Map.entry("word.minute.single", "minuto"),
    Map.entry("word.minute.plural", "minutos"),
    Map.entry("word.minute.short", "m"),
    Map.entry("word.hour.single", "hora"),
    Map.entry("word.hour.plural", "horas"),
    Map.entry("word.hour.short", "h"),
    Map.entry("word.day.single", "día"),
    Map.entry("word.day.plural", "días"),
    Map.entry("word.day.short", "d"),
    Map.entry("word.week.single", "semana"),
    Map.entry("word.week.plural", "semanas"),
    Map.entry("word.week.short", "sem"),
    Map.entry("word.month.single", "mes"),
    Map.entry("word.month.plural", "meses"),
    Map.entry("word.month.short", "me"),
    Map.entry("word.year.single", "año"),
    Map.entry("word.year.plural", "años"),
    Map.entry("word.year.short", "a"),
    Map.entry("word.time-period.all-time", "Siempre"),
    Map.entry("word.time-period.daily", "Diario"),
    Map.entry("word.time-period.weekly", "Semanal"),
    Map.entry("word.time-period.monthly", "Mensual")
  ));

  private TimeFormatter formatter;

  @BeforeEach
  public void setup() {
    MessageHandler messageHandler = MessageHandler.of((entity, path)
      -> MESSAGE_SOURCE.get(path));
    this.formatter = new TimeFormatter(messageHandler);
  }

  @Test
  void testFormatNoWordsLessThanHour() {
    String result = formatter.formatNoWords(30 * 1000L); // 30 segundos
    assertEquals("00:30", result);
  }

  @Test
  void testFormatNoWordsMoreThanHour() {
    String result = formatter.formatNoWords(2 * 3600 * 1000L + 15 * 60 * 1000L + 10 * 1000L);
    assertEquals("02:15:10", result);
  }

  @Test
  void testFormatBelowOneSecond() {
    String result = formatter.format(500);
    assertEquals("0.50 segundos", result);
  }

  @Test
  void testFormatFullWords() {
    long millis = 2L * 60 * 60 * 1000 + 30L * 60 * 1000 + 15L * 1000;
    String result = formatter.format(millis, false);

    assertTrue(result.contains("2h"));
    assertTrue(result.contains("30m"));
    assertTrue(result.contains("15s"));
  }

  @Test
  void testParseValid() {
    OptionalLong parsed = formatter.parse("1h30m15s");
    assertTrue(parsed.isPresent());
    assertEquals(60 * 60 * 1000 + 30 * 60 * 1000 + 15 * 1000, parsed.getAsLong());
  }

  @Test
  void testParseInvalid() {
    OptionalLong parsed = formatter.parse("1x30y");
    assertTrue(parsed.isEmpty());
  }

  @Test
  void testParseWithSpaces() {
    OptionalLong parsed = formatter.parse("1h 30m 15s");
    assertTrue(parsed.isPresent());
    assertEquals(60 * 60 * 1000 + 30 * 60 * 1000 + 15 * 1000, parsed.getAsLong());
  }
}
