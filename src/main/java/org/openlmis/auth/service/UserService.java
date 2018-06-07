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


import static org.openlmis.auth.i18n.MessageKeys.ACCOUNT_CREATED_EMAIL_SUBJECT;
import static org.openlmis.auth.i18n.MessageKeys.EMAIL_VERIFICATION_EMAIL_BODY;
import static org.openlmis.auth.i18n.MessageKeys.EMAIL_VERIFICATION_EMAIL_SUBJECT;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_REFERENCE_DATA_USER_NOT_FOUND;
import static org.openlmis.auth.i18n.MessageKeys.PASSWORD_RESET_EMAIL_BODY;
import static org.openlmis.auth.i18n.MessageKeys.PASSWORD_RESET_EMAIL_SUBJECT;

import java.time.ZonedDateTime;
import java.util.function.Function;
import org.openlmis.auth.domain.EmailVerificationToken;
import org.openlmis.auth.domain.ExpirationToken;
import org.openlmis.auth.domain.PasswordResetToken;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.dto.referencedata.UserDto;
import org.openlmis.auth.exception.ValidationMessageException;
import org.openlmis.auth.i18n.ExposedMessageSource;
import org.openlmis.auth.repository.EmailVerificationTokenRepository;
import org.openlmis.auth.repository.ExpirationTokenRepository;
import org.openlmis.auth.repository.PasswordResetTokenRepository;
import org.openlmis.auth.repository.UserRepository;
import org.openlmis.auth.service.notification.NotificationService;
import org.openlmis.auth.service.referencedata.UserReferenceDataService;
import org.openlmis.util.NotificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserService {
  public static final long TOKEN_VALIDITY_HOURS = 12;

  static final String RESET_PASSWORD_URL = System.getenv("BASE_URL") + "/#!/resetPassword/";
  static final String VERIFY_EMAIL_URL = System.getenv("BASE_URL") + "/verifyEmail/";

  private static final String MAIL_ADDRESS = System.getenv("MAIL_ADDRESS");

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @Autowired
  private EmailVerificationTokenRepository emailVerificationTokenRepository;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private ExposedMessageSource messageSource;

  /**
   * Creates a new user or updates an existing one.
   *
   * @param user user to be saved.
   * @return saved user.
   */
  public User saveUser(User user) {
    permissionService.canManageUsers();

    UserDto referenceDataUser = userReferenceDataService.findOne(user.getReferenceDataUserId());
    if (referenceDataUser == null) {
      throw new ValidationMessageException(ERROR_REFERENCE_DATA_USER_NOT_FOUND);
    }
    user.setReferenceDataUserId(referenceDataUser.getId());

    User dbUser = userRepository.findOneByReferenceDataUserId(user.getReferenceDataUserId());
    boolean isNewUser = dbUser == null;

    if (!isNewUser) {
      dbUser.setUsername(user.getUsername());
      dbUser.setEnabled(user.getEnabled());
    } else {
      dbUser = user;
    }

    String newPassword = user.getPassword();
    if (StringUtils.hasText(newPassword)) {
      BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
      dbUser.setPassword(encoder.encode(newPassword));
    }
    dbUser = userRepository.save(dbUser);

    if (isNewUser && referenceDataUser.hasEmail()) {
      sendEmailVerificationEmail(dbUser, referenceDataUser.getEmail());
    }

    return dbUser;
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

  /**
   * Creates (and deletes existing) email verification token for given user.
   *
   * @param user token's user
   * @return email verification token
   */
  private EmailVerificationToken createEmailVerificationToken(User user) {
    return createExpirationToken(user, emailVerificationTokenRepository, arg -> {
      EmailVerificationToken token = new EmailVerificationToken();
      token.setUser(arg);
      token.setExpiryDate(ZonedDateTime.now().plusHours(TOKEN_VALIDITY_HOURS));

      return token;
    });
  }

  private <T extends ExpirationToken> T createExpirationToken(User user,
      ExpirationTokenRepository<T> repository, Function<User, T> creator) {
    T token = repository.findOneByUser(user);

    if (token != null) {
      repository.delete(token);
      // the JPA provider feels free to reorganize and/or optimize the database writes of the
      // pending changes from the persistent context, in particular the JPA provider does not
      // feel obliged to perform the database writes in the ordering and form implicated by
      // the individual changes of the persistent context.

      // the flush() flushes the changes to the database so when the flush() is executed after
      // delete(), sql gets executed and the following save will have no problems.
      repository.flush();
    }

    return repository.save(creator.apply(user));
  }

  /**
   * Sends password reset email.
   *
   * @param user      the user whose password is being reset
   * @param email     recipient's email address
   * @param isNewUser whether the user was just created
   */
  public void sendResetPasswordEmail(User user, String email, boolean isNewUser) {
    PasswordResetToken token = createPasswordResetToken(user);

    String subjectMessageKey = (isNewUser) ? ACCOUNT_CREATED_EMAIL_SUBJECT
        : PASSWORD_RESET_EMAIL_SUBJECT;

    sendEmail(user, email, token, subjectMessageKey, PASSWORD_RESET_EMAIL_BODY, RESET_PASSWORD_URL);
  }

  /**
   * Sends email verification email.
   *
   * @param user      the user whose email is being verify
   * @param email     recipient's email address
   */
  private void sendEmailVerificationEmail(User user, String email) {
    EmailVerificationToken token = createEmailVerificationToken(user);
    sendEmail(
        user, email, token,
        EMAIL_VERIFICATION_EMAIL_SUBJECT, EMAIL_VERIFICATION_EMAIL_BODY,
        VERIFY_EMAIL_URL
    );
  }

  private void sendEmail(User user, String email, ExpirationToken token,
      String subjectKey, String bodyKey, String bodyUrl) {
    UserDto referenceDataUser = userReferenceDataService.findOne(user.getReferenceDataUserId());

    String[] bodyMsgArgs = {
        referenceDataUser.getFirstName(),
        referenceDataUser.getLastName(),
        bodyUrl + token.getId().toString()
    };
    String[] subjectMsgArgs = {};

    notificationService.send(new NotificationRequest(
        MAIL_ADDRESS,
        email,
        messageSource.getMessage(subjectKey, subjectMsgArgs, LocaleContextHolder.getLocale()),
        messageSource.getMessage(bodyKey, bodyMsgArgs, LocaleContextHolder.getLocale())));
  }
}
