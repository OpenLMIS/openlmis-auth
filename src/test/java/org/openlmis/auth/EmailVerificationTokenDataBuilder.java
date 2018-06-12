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

package org.openlmis.auth;

import java.time.ZonedDateTime;
import java.util.UUID;
import org.openlmis.auth.domain.EmailVerificationToken;
import org.openlmis.auth.domain.User;

public class EmailVerificationTokenDataBuilder {
  private UUID id = UUID.randomUUID();
  private ZonedDateTime expiryDate = ZonedDateTime.now().plusHours(12);
  private User user = new UserDataBuilder().build();
  private String email = "example@test.org";

  public EmailVerificationTokenDataBuilder withoutId() {
    this.id = null;
    return this;
  }

  public EmailVerificationTokenDataBuilder withUser(User user) {
    this.user = user;
    return this;
  }

  public EmailVerificationTokenDataBuilder withExpiredDate() {
    this.expiryDate = ZonedDateTime.now().minusDays(5);
    return this;
  }

  /**
   * Builds instance of {@link EmailVerificationToken} without id.
   */
  public EmailVerificationToken build() {
    EmailVerificationToken token = new EmailVerificationToken();
    token.setId(id);
    token.setExpiryDate(expiryDate);
    token.setUser(user);
    token.setEmail(email);

    return token;
  }
}
