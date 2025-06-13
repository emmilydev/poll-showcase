package dev.emmily.polls.test;

import dev.emmily.polls.util.time.TimeFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;

public class TimeFormatterTest {
  @Test
  public void test_formatNoWords() {
    LocalDateTime time = LocalDateTime.of(2025, Month.JUNE, 13, 3, 47, 32);

    Assertions.assertEquals("3h47m32s", TimeFormatter.format(time
      .atZone(ZoneOffset.systemDefault())
      .toInstant()
      .toEpochMilli()));
  }
}
