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

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseCommunicationServiceTest {
  private static final String TOKEN = UUID.randomUUID().toString();
  private static final String TOKEN_HEADER = "Bearer " + TOKEN;

  @Mock
  protected RestTemplate restTemplate;

  @Mock
  private AccessTokenService accessTokenService;

  @Captor
  protected ArgumentCaptor<URI> uriCaptor;

  @Before
  public void setUp() {
    when(accessTokenService.obtainToken("trusted-client", "secret")).thenReturn(TOKEN);
  }

  @After
  public void tearDown() {
    verify(accessTokenService).obtainToken("trusted-client", "secret");
  }

  protected abstract BaseCommunicationService getService();

  protected BaseCommunicationService prepareService() {
    BaseCommunicationService service = getService();
    service.setRestTemplate(restTemplate);
    service.setAccessTokenService(accessTokenService);

    ReflectionTestUtils.setField(service, "clientId", "trusted-client");
    ReflectionTestUtils.setField(service, "clientSecret", "secret");

    return service;
  }

  protected void assertAuthHeader(HttpEntity entity) {
    assertThat(entity.getHeaders().get(HttpHeaders.AUTHORIZATION),
            is(singletonList(TOKEN_HEADER)));
  }
}
