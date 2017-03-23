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

package org.openlmis.auth.dto.referencedata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minidev.json.JSONObject;

import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
public class UserDto extends BaseDto {

  @Getter
  @Setter
  private String username;

  @Getter
  @Setter
  private String firstName;

  @Getter
  @Setter
  private String lastName;

  @Getter
  @Setter
  private String email;

  @Getter
  @Setter
  private String timezone;

  @Getter
  private JSONObject homeFacility;

  @Getter
  @Setter
  private String homeFacilityCode;

  @Getter
  @Setter
  private boolean verified;

  @Getter
  @Setter
  private boolean active;

  @Getter
  @Setter
  private boolean loginRestricted;

  @Getter
  @Setter
  private Boolean allowNotify;

  @Getter
  @Setter
  private Map<String, String> extraData;

  @Getter
  @Setter
  private Set<RoleAssignmentDto> roleAssignments;
}
