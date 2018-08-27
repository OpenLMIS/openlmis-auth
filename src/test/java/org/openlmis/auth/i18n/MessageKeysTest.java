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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_API_KEY_NOT_FOUND;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_CLIENT_NOT_FOUND;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_RIGHT_NOT_FOUND;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_SEND_REQUEST;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_TOKEN_EXPIRED;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_TOKEN_INVALID;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_TOKEN_REQUIRED;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_USER_NOT_FOUND;
import static org.openlmis.auth.i18n.MessageKeys.PASSWORD_RESET_EMAIL_BODY;
import static org.openlmis.auth.i18n.MessageKeys.PASSWORD_RESET_EMAIL_SUBJECT;
import static org.openlmis.auth.i18n.MessageKeys.USERS_LOGOUT_CONFIRMATION;
import static org.openlmis.auth.i18n.MessageKeys.USER_NOT_FOUND;
import static org.openlmis.auth.i18n.MessageKeys.USER_NOT_FOUND_BY_EMAIL;

import org.junit.Test;

public class MessageKeysTest {

  @Test
  public void shouldContainsCorrectKeys() {
    verifyValue(ERROR_SEND_REQUEST, "error.sendRequest");

    verifyValue(ERROR_TOKEN_INVALID, "auth.error.token.invalid");
    verifyValue(ERROR_TOKEN_EXPIRED, "auth.error.token.expired");
    verifyValue(ERROR_TOKEN_REQUIRED, "auth.error.token.required");

    verifyValue(ERROR_CLIENT_NOT_FOUND, "auth.error.client.notFound");

    verifyValue(ERROR_API_KEY_NOT_FOUND, "auth.error.apiKey.notFound");

    verifyValue(ERROR_USER_NOT_FOUND, "auth.error.authentication.userCanNotBeFound");
    verifyValue(ERROR_RIGHT_NOT_FOUND, "auth.error.authentication.rightCanNotBeFound");
    verifyValue(ERROR_NO_FOLLOWING_PERMISSION, "auth.error.authorization.noFollowingPermission");

    verifyValue(PASSWORD_RESET_EMAIL_SUBJECT, "auth.email.resetPassword.subject");
    verifyValue(PASSWORD_RESET_EMAIL_BODY, "auth.email.resetPassword.body");

    verifyValue(USER_NOT_FOUND, "users.notFound");

    verifyValue(USER_NOT_FOUND_BY_EMAIL, "users.notFoundByEmail");

    verifyValue(USERS_LOGOUT_CONFIRMATION, "users.logout.confirmation");
  }

  private void verifyValue(String actual, String expected) {
    assertThat(actual, is(equalTo(expected)));
  }

}
