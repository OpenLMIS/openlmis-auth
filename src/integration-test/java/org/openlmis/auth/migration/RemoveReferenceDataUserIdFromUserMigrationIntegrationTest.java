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

package org.openlmis.auth.migration;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class RemoveReferenceDataUserIdFromUserMigrationIntegrationTest
    extends BaseMigrationIntegrationTest {

  @Override
  void insertDataBeforeMigration() {
    addData(true);
    addData(false);
    addData(true);
    addData(false);
    addData(true);
  }

  private void addData(boolean withPasswordResetToken) {
    Map<String, Object> user = generateUser();
    save(TABLE_AUTH_USERS, user);

    if (withPasswordResetToken) {
      save(TABLE_PASSWORD_RESET_TOKENS, generatePasswordResetToken(user.get("id")));
    }
  }

  @Override
  String getTargetBeforeTestMigration() {
    return "20180320172415321";
  }

  @Override
  String getTestMigrationTarget() {
    return "20180613120215847";
  }

  @Override
  void verifyDataAfterMigration() {
    List<Map<String, Object>> users = getRows(TABLE_AUTH_USERS);
    List<Map<String, Object>> passwordResetTokens = getRows(TABLE_PASSWORD_RESET_TOKENS);

    assertThat(users)
        .hasSize(6) // one from bootstrap
        .extracting(user -> user.get("referencedatauserid"))
        .contains(null, null, null, null, null, null);

    assertThat(passwordResetTokens).hasSize(3);

    for (Map<String, Object> passwordResetToken : passwordResetTokens) {
      Object userId = passwordResetToken.get("userid");
      Map<String, Object> user = users
          .stream()
          .filter(elem -> userId.equals(elem.get("id")))
          .findFirst()
          .orElse(null);

      assertThat(user).isNotNull();
    }
  }

  private Map<String, Object> generateUser() {
    return ImmutableMap
        .<String, Object>builder()
        .put("id", UUID.randomUUID())
        .put("username", "user" + getNextInstanceNumber())
        .put("password", new BCryptPasswordEncoder().encode("password"))
        .put("enabled", true)
        .put("referencedatauserid", UUID.randomUUID())
        .build();
  }

  private Map<String, Object> generatePasswordResetToken(Object userId) {
    return ImmutableMap
        .<String, Object>builder()
        .put("id", UUID.randomUUID())
        .put("expiryDate", ZonedDateTime
            .now().plusDays(5).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        .put("userId", userId)
        .build();
  }
}
