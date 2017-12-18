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

import static java.time.Clock.systemUTC;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;

import org.openlmis.auth.domain.ApiKey;

import java.util.UUID;

public class ApiKeyDataBuilder {
  private UUID id;
  private String clientId;
  private UUID serviceAccountId;

  /**
   * Create new instance of {@link ApiKeyDataBuilder}.
   */
  public ApiKeyDataBuilder() {
    id = UUID.randomUUID();
    clientId = "api-key-client-" + now(systemUTC()).format(ofPattern("yyyyMMddHHmmssSSS"));
    serviceAccountId = UUID.randomUUID();
  }

  /**
   * Creates new fresh instance of {@link ApiKey}.
   */
  public ApiKey buildAsNew() {
    return new ApiKey(clientId, serviceAccountId);
  }

  /**
   * Creates new instance of {@link ApiKey} with value for id field.
   */
  public ApiKey build() {
    ApiKey key = buildAsNew();
    key.setId(id);

    return key;
  }
}
