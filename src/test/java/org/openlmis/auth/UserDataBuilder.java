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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.openlmis.auth.domain.User;

public class UserDataBuilder {
  private static final AtomicInteger instanceNumber = new AtomicInteger();

  private UUID id = UUID.randomUUID();
  private String username = "user" + instanceNumber.incrementAndGet();
  private boolean enabled = true;
  private String password = null;

  public UserDataBuilder withPassword(String password) {
    this.password = password;
    return this;
  }

  /**
   * Builds instance of {@link User} without id.
   */
  public User build() {
    User user = new User();
    user.setId(id);
    user.setUsername(username);
    user.setEnabled(enabled);
    user.setPassword(password);

    return user;
  }

}
