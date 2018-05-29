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

package org.openlmis.auth.util;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.openlmis.auth.dto.referencedata.RoleAssignmentDto;
import org.openlmis.auth.dto.referencedata.UserDto;

public class UserDataBuilder {
  private UUID id = UUID.randomUUID();
  private String username = RandomStringUtils.randomAlphanumeric(5);
  private String firstName = RandomStringUtils.randomAlphanumeric(5);
  private String lastName = RandomStringUtils.randomAlphanumeric(5);
  private String email = username + "@some.where";
  private boolean verified = true;
  private UUID homeFacilityId = UUID.randomUUID();
  private Set<RoleAssignmentDto> roleAssignments = Collections.emptySet();
  private Boolean allowNotify = true;
  private boolean active = true;
  private boolean loginRestricted = false;
  private String timezone = "UTC";
  private Map<String, String> extraData;

  /**
   * Creates new instance of {@link UserDto}.
   */
  public UserDto build() {
    UserDto user = new UserDto(
        username, firstName, lastName, email, timezone, homeFacilityId, verified, active,
        loginRestricted, allowNotify, extraData, roleAssignments
    );

    user.setId(id);

    return user;
  }

}
