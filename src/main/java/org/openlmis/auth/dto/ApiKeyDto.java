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

import org.openlmis.auth.domain.ApiKey;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public final class ApiKeyDto implements ApiKey.Importer, ApiKey.Exporter {
  private UUID token;
  private UUID createdBy;
  private ZonedDateTime createdDate;

  /**
   * Creates new instance of {@link ApiKeyDto} based on passed service account.
   */
  public static ApiKeyDto newInstance(ApiKey apiKey) {
    ApiKeyDto dto = new ApiKeyDto();
    apiKey.export(dto);

    return dto;
  }
}
