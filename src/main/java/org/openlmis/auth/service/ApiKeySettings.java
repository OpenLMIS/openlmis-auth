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

package org.openlmis.auth.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiKeySettings {
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
      .ofPattern("yyyyMMddHHmmssSSS");

  @Value("${auth.server.clientId.apiKey.prefix}")
  private String prefix;

  /**
   * Generates a new API Key client ID.
   */
  public String generateClientId() {
    LocalDateTime currentDate = LocalDateTime.now(Clock.systemUTC());
    String currentDateString = currentDate.format(DATE_TIME_FORMATTER);

    return prefix + currentDateString;
  }

  public boolean isApiKey(String clientId) {
    return StringUtils.startsWith(clientId, prefix);
  }

}
