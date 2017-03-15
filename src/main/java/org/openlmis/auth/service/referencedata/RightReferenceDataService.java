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
import org.openlmis.auth.dto.RightDto;
import org.openlmis.auth.service.BaseCommunicationService;
import org.openlmis.auth.service.RequestParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RightReferenceDataService extends BaseCommunicationService<RightDto> {

  @Getter
  @Value("${service.url}")
  private String serviceUrl;


  @Override
  protected String getUrl() {
    return "/api/rights/";
  }

  @Override
  protected Class<RightDto> getResultClass() {
    return RightDto.class;
  }

  @Override
  protected Class<RightDto[]> getArrayResultClass() {
    return RightDto[].class;
  }

  /**
   * Find a correct right by the provided name.
   *
   * @param name right name
   * @return right related with the name or {@code null}.
   */
  public RightDto findRight(String name) {
    List<RightDto> rights = findAll("search", RequestParameters.init().set("name", name));
    return rights.isEmpty() ? null : rights.get(0);
  }

}
