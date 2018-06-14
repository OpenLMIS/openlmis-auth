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

import static java.util.UUID.randomUUID;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.UUID;
import org.openlmis.auth.dto.referencedata.RoleAssignmentDto;
import org.openlmis.auth.dto.referencedata.UserMainDetailsDto;

public final class DummyUserMainDetailsDto extends UserMainDetailsDto {
  public static final String REFERENCE_ID = "35316636-6264-6331-2d34-3933322d3462";
  public static final String USERNAME = "admin";
  public static final String PASSWORD = "password";
  public static final String EMAIL = "test@openlmis.org";

  /**
   * Creates new instance of dummy user dto.
   */
  public DummyUserMainDetailsDto() {
    super(
        USERNAME, "Admin", "User", EMAIL, null, null, null, null, true,
        false, false, true, null, getDummyRoleAssignments()
    );
    setId(UUID.fromString(REFERENCE_ID));
  }

  private static Set<RoleAssignmentDto> getDummyRoleAssignments() {
    return Sets.newHashSet(
        new RoleAssignmentDto(randomUUID(), null, null, null), // general
        new RoleAssignmentDto(randomUUID(), randomUUID(), randomUUID(), null), // supervision
        new RoleAssignmentDto(randomUUID(), null, null, randomUUID()) // fulfillment
    );
  }

}
