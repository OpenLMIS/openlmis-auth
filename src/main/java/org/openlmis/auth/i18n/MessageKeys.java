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

  public static final String ERROR_SEND_REQUEST = ERROR + ".sendRequest";

  public static final String ERROR_TOKEN_INVALID = ERROR_PREFIX + ".token.invalid";
  public static final String ERROR_TOKEN_EXPIRED = ERROR_PREFIX + ".token.expired";

  public static final String ERROR_CLIENT_NOT_FOUND = ERROR_PREFIX + ".client.notFound";

  public static final String ERROR_API_KEY_FOUND = ERROR_PREFIX + ".apiKey.found";
  public static final String ERROR_API_KEY_NOT_FOUND = ERROR_PREFIX + ".apiKey.notFound";

  public static final String ERROR_USER_NOT_FOUND = ERROR_PREFIX
      + ".authentication.userCanNotBeFound";
  public static final String ERROR_RIGHT_NOT_FOUND = ERROR_PREFIX
      + ".authentication.rightCanNotBeFound";
  public static final String ERROR_NO_FOLLOWING_PERMISSION = ERROR_PREFIX
      + ".authorization.noFollowingPermission";

  public static final String ERROR_REFERENCE_DATA_USER_NOT_FOUND = ERROR_PREFIX
      + ".users.referenceDataUserNotFound";

  public static final String ACCOUNT_CREATED_EMAIL_SUBJECT = SERVICE_PREFIX
      + ".email.accountCreated.subject";
  public static final String PASSWORD_RESET_EMAIL_SUBJECT = SERVICE_PREFIX
      + ".email.resetPassword.subject";
  public static final String PASSWORD_RESET_EMAIL_BODY = SERVICE_PREFIX
      + ".email.resetPassword.body";

  public static final String USERS_PASSWORD_RESET_CONFIRMATION = USERS
      + ".passwordReset.confirmation";
  public static final String USERS_PASSWORD_RESET_USER_NOT_FOUND = USERS
      + ".passwordReset.userNotFound";

  public static final String USERS_FORGOT_PASSWORD_USER_NOT_FOUND = USERS
      + ".forgotPassword.userNotFound";

  public static final String USERS_LOGOUT_CONFIRMATION = USERS + ".logout.confirmation";

  public static final String ERROR_CONSTRAINT = ERROR_PREFIX + ".constraint";


  private MessageKeys() {
    throw new UnsupportedOperationException();
  }

}
