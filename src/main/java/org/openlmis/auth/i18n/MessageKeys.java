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
  private static final String SERVICE_PREFIX = "auth";
  private static final String ERROR_PREFIX = SERVICE_PREFIX + ".error";

  public static final String ERROR_USER_NOT_FOUND = ERROR_PREFIX
      + ".authentication.userCanNotBeFound";
  public static final String ERROR_RIGHT_NOT_FOUND = ERROR_PREFIX
      + ".authentication.rightCanNotBeFound";
  public static final String ERROR_NO_FOLLOWING_PERMISSION = ERROR_PREFIX
      + ".authorization.noFollowingPermission";
  public static final String ERROR_REFERENCE_DATA_USER_NOT_FOUND = ERROR_PREFIX
          + ".users.referenceDataUserNotFound";
  public static final String ACCOUNT_CREATED_EMAIL_SUBJECT =
      SERVICE_PREFIX + "email.accountCreated.subject";
  public static final String PASSWORD_RESET_EMAIL_SUBJECT =
      SERVICE_PREFIX + "email.resetPassword.subject";
  public static final String PASSWORD_RESET_EMAIL_BODY =
      SERVICE_PREFIX + "email.resetPassword.body";

  private MessageKeys() {
    throw new UnsupportedOperationException();
  }
}
