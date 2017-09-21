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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.auth.exception.ConfigurationSettingException;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationSettingServiceTest {

  @InjectMocks
  private ConfigurationSettingService configurationSettingService;

  @Test
  public void shouldReturnConsulProtocol() {
    // given
    String expected = "https";
    ReflectionTestUtils.setField(configurationSettingService, "consulProtocol", expected);

    // when
    String result = configurationSettingService.getConsulProtocol();

    // then
    assertEquals(expected, result);
  }

  @Test
  public void shouldReturnConsulHost() {
    // given
    String expected = "consul";
    ReflectionTestUtils.setField(configurationSettingService, "consulHost", expected);

    // when
    String result = configurationSettingService.getConsulHost();

    // then
    assertEquals(expected, result);
  }

  @Test
  public void shouldReturnConsulServicesUrl() {
    // given
    String expected = "/v1/catalog/services";
    ReflectionTestUtils.setField(
        configurationSettingService,
        "consulServicesUrl",
        expected
    );

    // when
    String result = configurationSettingService.getConsulServicesUrl();

    // then
    assertEquals(expected, result);
  }

  @Test
  public void shouldReturnConsulServicesTag() {
    // given
    String expected = "openlmis-service";
    ReflectionTestUtils.setField(configurationSettingService, "consulServiceTag", expected);

    // when
    String result = configurationSettingService.getConsulServiceTag();

    // then
    assertEquals(expected, result);
  }

  @Test
  public void shouldReturnConsulPort() {
    // given
    int expected = 8500;
    ReflectionTestUtils.setField(
        configurationSettingService,
        "consulPort",
        String.valueOf(expected)
    );

    // when
    int result = configurationSettingService.getConsulPort();

    // then
    assertEquals(expected, result);
  }

  @Test(expected = ConfigurationSettingException.class)
  public void shouldThrowExceptionIfNumericValueIsInvalid() {
    // given
    ReflectionTestUtils.setField(configurationSettingService, "consulPort", "port");

    // when
    configurationSettingService.getConsulPort();
  }
}
