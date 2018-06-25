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

package org.openlmis.auth.service;


import static org.openlmis.auth.i18n.MessageKeys.PASSWORD_RESET_EMAIL_BODY;
import static org.openlmis.auth.i18n.MessageKeys.PASSWORD_RESET_EMAIL_SUBJECT;

import java.time.ZonedDateTime;
import org.openlmis.auth.domain.PasswordResetToken;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetNotifier extends ExpirationTokenNotifier<PasswordResetToken> {
  static final String RESET_PASSWORD_URL = System.getenv("BASE_URL") + "/#!/resetPassword/";

  @Autowired
  private PasswordResetTokenRepository passwordResetTokenRepository;

  /**
   * Sends password reset email.
   *
   * @param user      the user whose password is being reset
   */
  public void sendNotification(User user) {
    PasswordResetToken token = createPasswordResetToken(user);

    sendEmail(
        user, token, PASSWORD_RESET_EMAIL_SUBJECT, PASSWORD_RESET_EMAIL_BODY, RESET_PASSWORD_URL
    );
  }

  /**
   * Creates (and deletes existing) password reset token for given user.
   *
   * @param user token's user
   * @return password reset token
   */
  public PasswordResetToken createPasswordResetToken(User user) {
    return createExpirationToken(user, passwordResetTokenRepository, arg -> {
      PasswordResetToken token = new PasswordResetToken();
      token.setUser(arg);
      token.setExpiryDate(ZonedDateTime.now().plusHours(TOKEN_VALIDITY_HOURS));

      return token;
    });
  }

}
