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

package org.openlmis.auth.service.consul;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.openlmis.auth.service.consul.ConsulCommunicationService.SERVICE_SEPARATOR;

import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.auth.domain.Client;
import org.openlmis.auth.dto.consul.ServicesListDto;
import org.openlmis.auth.repository.ClientRepository;
import org.openlmis.auth.service.ConfigurationSettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class ConsulCommunicationServiceTest {

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private ClientRepository clientRepository;

  @Mock
  private ConfigurationSettingService configurationSettingService;

  @InjectMocks
  private ConsulCommunicationService consulCommunicationService;

  @Before
  public void setUp() {
    consulCommunicationService.setRestTemplate(restTemplate);
  }

  @Test
  public void shouldUpdateOAuthResourcesWhenResourcesChanged() {
    // given
    mockConfigurationSettings();

    List<String> validServices = Arrays.asList("auth", "requisition");
    List<String> invalidServices = Collections.singletonList("random-service");
    ServicesListDto expectedBody = generateServicesList(validServices, invalidServices);
    mockExternalResponse(expectedBody);

    Client client = new Client();
    client.setResourceIds("auth,referencedata");
    mockRepositoryClients(client);

    // when
    consulCommunicationService.updateOAuthResources();

    // then
    verify(clientRepository, atLeastOnce()).save(eq(client));

    Set<String> expectedResources = new HashSet<>(validServices);
    Set<String> clientResources = Sets.newHashSet(
        client.getResourceIds().split(SERVICE_SEPARATOR));

    assertEquals(expectedResources, clientResources);
  }

  @Test
  public void shouldNotUpdateOAuthResourcesWhenResourcesNotChanged() {
    // given
    mockConfigurationSettings();

    ServicesListDto expectedBody = generateServicesList(
        Collections.singletonList("referencedata"), Collections.singletonList("random-service"));
    mockExternalResponse(expectedBody);

    Client client = new Client();
    client.setResourceIds("referencedata");
    mockRepositoryClients(client);

    // when
    consulCommunicationService.updateOAuthResources();

    // then
    verify(clientRepository, never()).save(eq(client));

  }

  private void mockConfigurationSettings() {
    given(configurationSettingService.getConsulServicesUrl()).willReturn("/v1/catalog/services");
    given(configurationSettingService.getConsulServiceTag()).willReturn("openlims-service");
    given(configurationSettingService.getConsulProtocol()).willReturn("http");
    given(configurationSettingService.getConsulHost()).willReturn("consul");
    given(configurationSettingService.getConsulPort()).willReturn(8500);
  }

  private void mockRepositoryClients(Client... clients) {
    given(clientRepository.findAll()).willReturn(Arrays.asList(clients));
  }

  private void mockExternalResponse(ServicesListDto body) {
    String expectedUrl = generateExpectedUrl();

    ResponseEntity<ServicesListDto> expectedResponse = mock(ResponseEntity.class);
    given(expectedResponse.getBody()).willReturn(body);

    given(restTemplate.getForEntity(expectedUrl, ServicesListDto.class))
        .willReturn(expectedResponse);
  }

  private ServicesListDto generateServicesList(List<String> valid, List<String> invalid) {
    ServicesListDto services = new ServicesListDto();
    String serviceTag = configurationSettingService.getConsulServiceTag();

    for (String service : valid) {
      services.put(service, Collections.singletonList(serviceTag));
    }

    for (String service : invalid) {
      services.put(service, Collections.emptyList());
    }

    return services;
  }

  private String generateExpectedUrl() {
    String services = configurationSettingService.getConsulServicesUrl();
    String protocol = configurationSettingService.getConsulProtocol();
    String host = configurationSettingService.getConsulHost();
    int port = configurationSettingService.getConsulPort();

    return String.format("%s://%s:%d%s", protocol, host, port, services);
  }
}
