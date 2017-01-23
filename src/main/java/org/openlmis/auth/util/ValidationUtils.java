package org.openlmis.auth.util;

public abstract class ValidationUtils {
  public static boolean isNullOrWhitespace(String input) {
    return input == null || input.trim().isEmpty();
  }
}
