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

import java.time.ZonedDateTime;
import java.util.UUID;
import org.openlmis.auth.domain.ApiKey;
import org.openlmis.auth.domain.Client;
import org.openlmis.auth.domain.CreationDetails;

public class ApiKeyDataBuilder {
  private UUID token = UUID.randomUUID();
  private UUID createdBy = UUID.randomUUID();
  private Client client = new ClientDataBuilder().buildApiKeyClient();
  private ZonedDateTime createdDate = ZonedDateTime.now();

  /**
   * Builds instance of {@link ApiKey} without id.
   */
  public ApiKey build() {
    return new ApiKey(token, client, new CreationDetails(createdBy, createdDate));
  }
}
