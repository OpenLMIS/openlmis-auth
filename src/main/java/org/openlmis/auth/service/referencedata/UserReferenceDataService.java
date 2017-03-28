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

package org.openlmis.auth.service.referencedata;

import lombok.Getter;
import org.openlmis.auth.dto.ResultDto;
import org.openlmis.auth.dto.referencedata.UserDto;
import org.openlmis.auth.service.BaseCommunicationService;
import org.openlmis.auth.service.RequestParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserReferenceDataService extends BaseCommunicationService<UserDto> {

  @Getter
  @Value("${service.url}")
  private String serviceUrl;

  protected String getUrl() {
    return "/api/users/";
  }

  protected Class<UserDto> getResultClass() {
    return UserDto.class;
  }

  protected Class<UserDto[]> getArrayResultClass() {
    return UserDto[].class;
  }

  /**
   * This method retrieves a user with given name.
   *
   * @param name the name of user.
   * @return RefDUserDto containing user's data, or null if such user was not found.
   */
  public UserDto findUser(String name) {
    Map<String, Object> payload = Collections.singletonMap("username", name);

    Page<UserDto> users = getPage("search", Collections.emptyMap(), payload);
    return users.getContent().isEmpty() ? null : users.getContent().get(0);
  }

  /**
   * This method creates or updates a user.
   *
   * @param user the user to put.
   * @return RefDUserDto containing user's data.
   */
  public UserDto putUser(UserDto user) {
    return put("", user);
  }

  /**
   * Check if user has a right with certain criteria.
   *
   * @param user     id of user to check for right
   * @param right    right to check
   * @param program  program to check (for supervision rights, can be {@code null})
   * @param facility facility to check (for supervision rights, can be {@code null})
   * @return an instance of {@link ResultDto} with true or false depending on if user has the
   *         right.
   */
  public ResultDto<Boolean> hasRight(UUID user, UUID right, UUID program, UUID facility,
                                     UUID warehouse) {
    RequestParameters parameters = RequestParameters
        .init()
        .set("rightId", right)
        .set("programId", program)
        .set("facilityId", facility)
        .set("warehouseId", warehouse);
    
    return getResult(user + "/hasRight", parameters, Boolean.class);
  }
}
