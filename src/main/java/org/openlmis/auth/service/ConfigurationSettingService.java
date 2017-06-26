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

import org.openlmis.auth.exception.ConfigurationSettingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.NoArgsConstructor;

@Service
@NoArgsConstructor
public class ConfigurationSettingService {

  @Value("${consul.protocol}")
  private String consulProtocolSetting;

  @Value("${consul.host}")
  private String consulHostSetting;

  @Value("${consul.port}")
  private String consulPortSetting;

  @Value("${consul.services.url}")
  private String consulServicesUrlSetting;

  @Value("${consul.services.serviceTag}")
  private String consulServiceTagSetting;

  public String getConsulProtocol() {
    return consulProtocolSetting;
  }

  public String getConsulHost() {
    return consulHostSetting;
  }

  public int getConsulPort() {
    return getInteger(consulPortSetting);
  }

  public String getConsulServicesUrl() {
    return consulServicesUrlSetting;
  }

  public String getConsulServiceTag() {
    return consulServiceTagSetting;
  }

  private int getInteger(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException err) {
      throw new ConfigurationSettingException(err);
    }
  }
}
