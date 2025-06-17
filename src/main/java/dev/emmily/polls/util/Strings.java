package dev.emmily.polls.util;

public interface Strings {
  static String valueOrDef(String value, String def) {
    return value != null ? value : def;
  }
}
