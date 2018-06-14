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

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserMainDetailsDto extends BaseDto {
  private String username;
  private String firstName;
  private String lastName;
  private String email;
  private String jobTitle;
  private String phoneNumber;
  private String timezone;
  private UUID homeFacilityId;
  private boolean verified;
  private boolean active;
  private boolean loginRestricted;
  private Boolean allowNotify;
  private Map<String, String> extraData;
  private Set<RoleAssignmentDto> roleAssignments = Sets.newHashSet();

  public boolean hasEmail() {
    return isNotBlank(email);
  }
}
