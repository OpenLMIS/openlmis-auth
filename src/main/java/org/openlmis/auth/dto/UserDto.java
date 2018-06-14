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

package org.openlmis.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.dto.referencedata.UserMainDetailsDto;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class UserDto extends UserMainDetailsDto {

  @Getter
  @Setter
  private String password;

  @Getter
  @Setter
  private Boolean enabled;

  /**
   * Creates a new instance based on data from auth and reference data users.
   */
  public UserDto(User user, UserMainDetailsDto referenceDataUser) {
    super(
        referenceDataUser.getUsername(), referenceDataUser.getFirstName(),
        referenceDataUser.getLastName(), referenceDataUser.getEmail(),
        referenceDataUser.getJobTitle(), referenceDataUser.getPhoneNumber(),
        referenceDataUser.getTimezone(), referenceDataUser.getHomeFacilityId(),
        referenceDataUser.isVerified(), referenceDataUser.isActive(),
        referenceDataUser.isLoginRestricted(), referenceDataUser.getAllowNotify(),
        referenceDataUser.getExtraData(), referenceDataUser.getRoleAssignments()
    );

    setId(referenceDataUser.getId());
    this.password = user.getPassword();
    this.enabled = user.getEnabled();
  }

  /**
   * Gets a reference data user data part of the given user request body.
   */
  @JsonIgnore
  public UserMainDetailsDto getReferenceDataUser() {
    UserMainDetailsDto data = new UserMainDetailsDto(
        getUsername(), getFirstName(), getLastName(), getEmail(), getJobTitle(), getPhoneNumber(),
        getTimezone(), getHomeFacilityId(), isVerified(), isActive(), isLoginRestricted(),
        getAllowNotify(), getExtraData(), getRoleAssignments()
    );
    data.setId(getId());

    return data;
  }

}
