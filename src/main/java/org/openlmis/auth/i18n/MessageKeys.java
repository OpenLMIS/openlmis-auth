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

package org.openlmis.auth.i18n;

public abstract class MessageKeys {

  private static final String ERROR = "error";
  private static final String USERS = "users";

  private static final String SERVICE_PREFIX = "auth";
  private static final String ERROR_PREFIX = SERVICE_PREFIX + "." + ERROR;

  public static final String ERROR_FIELD_IS_INVARIANT = ERROR_PREFIX + ".fieldIsInvariant";
  public static final String ERROR_USERNAME_INVALID = ERROR_PREFIX + ".username.invalid";
  public static final String ERROR_FIELD_REQUIRED = ERROR_PREFIX + ".fieldRequired";

  public static final String ERROR_SEND_REQUEST = ERROR + ".sendRequest";
  public static final String ERROR_IO = ERROR_PREFIX + ".io";

  public static final String ERROR_TOKEN_INVALID = ERROR_PREFIX + ".token.invalid";
  public static final String ERROR_TOKEN_EXPIRED = ERROR_PREFIX + ".token.expired";
  public static final String ERROR_TOKEN_REQUIRED = ERROR_PREFIX + ".token.required";

  public static final String ERROR_CLIENT_NOT_FOUND = ERROR_PREFIX + ".client.notFound";

  public static final String ERROR_API_KEY_NOT_FOUND = ERROR_PREFIX + ".apiKey.notFound";

  public static final String ERROR_USER_NOT_FOUND = ERROR_PREFIX
      + ".authentication.userCanNotBeFound";
  public static final String ERROR_RIGHT_NOT_FOUND = ERROR_PREFIX
      + ".authentication.rightCanNotBeFound";
  public static final String ERROR_NO_FOLLOWING_PERMISSION = ERROR_PREFIX
      + ".authorization.noFollowingPermission";

  public static final String PASSWORD_RESET_EMAIL_SUBJECT = SERVICE_PREFIX
      + ".email.resetPassword.subject";
  public static final String PASSWORD_RESET_EMAIL_BODY = SERVICE_PREFIX
      + ".email.resetPassword.body";

  public static final String USERS_PASSWORD_RESET_INVALID_VALUE = USERS
      + ".passwordReset.invalidValue";

  public static final String USER_NOT_FOUND = USERS + ".notFound";

  public static final String USER_NOT_FOUND_BY_EMAIL = USERS + ".notFoundByEmail";

  public static final String USERS_LOGOUT_CONFIRMATION = USERS + ".logout.confirmation";

  public static final String ERROR_CONSTRAINT = ERROR_PREFIX + ".constraint";

  public static final String ERROR_CLIENT_NOT_SUPPORTED =
      ERROR_PREFIX + ".apiKey.create.clientUserNotSupported";

  public static final String ERROR_SEND_NOTIFICATION_FAILURE =
      ERROR_PREFIX + ".sendNotification.failure";

  private MessageKeys() {
    throw new UnsupportedOperationException();
  }

}
