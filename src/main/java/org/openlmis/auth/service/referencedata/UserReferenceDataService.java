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

import java.util.UUID;
import lombok.Getter;
import org.openlmis.auth.dto.ResultDto;
import org.openlmis.auth.dto.referencedata.UserMainDetailsDto;
import org.openlmis.auth.service.BaseCommunicationService;
import org.openlmis.auth.service.RequestParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserReferenceDataService extends BaseCommunicationService<UserMainDetailsDto> {

  @Getter
  @Value("${service.url}")
  private String serviceUrl;

  protected String getUrl() {
    return "/api/users/";
  }

  protected Class<UserMainDetailsDto> getResultClass() {
    return UserMainDetailsDto.class;
  }

  protected Class<UserMainDetailsDto[]> getArrayResultClass() {
    return UserMainDetailsDto[].class;
  }

  /**
   * Check if user has a right with certain criteria.
   *
   * @param user     id of user to check for right
   * @param right    right to check
   * @return an instance of {@link ResultDto} with true or false depending on if user has the
   *         right.
   */
  public ResultDto<Boolean> hasRight(UUID user, UUID right) {
    RequestParameters parameters = RequestParameters
        .init()
        .set("rightId", right);
    
    return getResult(user + "/hasRight", parameters, Boolean.class);
  }
}
