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

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openlmis.auth.exception.ConfigurationSettingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
public class ConfigurationSettingService {

  @Getter
  @Value("${consul.protocol}")
  private String consulProtocol;

  @Getter
  @Value("${consul.host}")
  private String consulHost;

  @Value("${consul.port}")
  private String consulPort;

  @Getter
  @Value("${consul.services.url}")
  private String consulServicesUrl;

  @Getter
  @Value("${consul.services.serviceTag}")
  private String consulServiceTag;

  public int getConsulPort() {
    return getInteger(consulPort);
  }

  private int getInteger(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException err) {
      throw new ConfigurationSettingException(err);
    }
  }
}
