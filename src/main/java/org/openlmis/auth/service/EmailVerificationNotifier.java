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


import static org.openlmis.auth.i18n.MessageKeys.EMAIL_VERIFICATION_EMAIL_BODY;
import static org.openlmis.auth.i18n.MessageKeys.EMAIL_VERIFICATION_EMAIL_SUBJECT;

import java.time.ZonedDateTime;
import org.openlmis.auth.domain.EmailVerificationToken;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.repository.EmailVerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationNotifier extends ExpirationTokenNotifier<EmailVerificationToken> {
  static final String VERIFY_EMAIL_URL = System.getenv("BASE_URL") + "/api/users/auth/verifyEmail/";

  @Autowired
  private EmailVerificationTokenRepository emailVerificationTokenRepository;

  /**
   * Sends email verification email.
   *
   * @param user      the user whose email is being verified
   * @param email     recipient's new email address
   */
  @Async
  public void sendNotification(User user, String email) {
    EmailVerificationToken token = createEmailVerificationToken(user, email);
    sendEmail(
        user, email, token,
        EMAIL_VERIFICATION_EMAIL_SUBJECT, EMAIL_VERIFICATION_EMAIL_BODY,
        VERIFY_EMAIL_URL
    );
  }

  /**
   * Creates (and deletes existing) email verification token for given user.
   *
   * @param user token's user
   * @return email verification token
   */
  private EmailVerificationToken createEmailVerificationToken(User user, String email) {
    return createExpirationToken(user, emailVerificationTokenRepository, arg -> {
      EmailVerificationToken token = new EmailVerificationToken();
      token.setUser(arg);
      token.setExpiryDate(ZonedDateTime.now().plusHours(TOKEN_VALIDITY_HOURS));
      token.setEmail(email);

      return token;
    });
  }

}
