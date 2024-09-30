/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.auth.web;

import com.nulabinc.zxcvbn.Feedback;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.logging.log4j.util.Strings;
import org.openlmis.auth.exception.ValidationMessageException;
import org.openlmis.auth.i18n.MessageKeys;
import org.openlmis.auth.util.Message;
import org.springframework.stereotype.Component;

@Component
public class PasswordStrengthValidator {

  private static final int MINIMAL_PASSWORD_STRENGTH_SCORE =
      PasswordStrengthScore.GOOD.getScore();

  /**
   * Checks the strength of the given password.
   *
   * @param password the password to check.
   * @throws ValidationMessageException if the password's strength is below the required threshold.
   */
  public void verifyPasswordStrength(String password) {
    Zxcvbn zxcvbn = new Zxcvbn();
    Strength strength = zxcvbn.measure(password);

    if (strength.getScore() < MINIMAL_PASSWORD_STRENGTH_SCORE) {
      Feedback feedback = strength.getFeedback();
      List<String> suggestions = feedback.getSuggestions();
      String suggestionsMessage = buildSuggestionsMessage(suggestions);

      // feedback.warning can sometimes be an empty string
      String reason = (feedback.getWarning() == null || feedback.getWarning().isEmpty())
          ? Strings.EMPTY : feedback.getWarning() + " ";

      throw new ValidationMessageException(new Message(MessageKeys.USERS_PASSWORD_TOO_WEAK,
          reason,
          suggestionsMessage));
    }
  }

  private String buildSuggestionsMessage(List<String> suggestions) {
    if (suggestions.isEmpty()) {
      return Strings.EMPTY;
    }
    String suggestionPrefix = suggestions.size() > 1 ? "Suggestions: " : "Suggestion: ";
    return suggestionPrefix + String.join(", ", suggestions);
  }

  @AllArgsConstructor
  @Getter
  enum PasswordStrengthScore {
    WEAK(0), FAIR(1), GOOD(2), STRONG(3), VERY_STRONG(4);
    private final int score;
  }

}
