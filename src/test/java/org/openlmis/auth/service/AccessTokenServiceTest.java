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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.openlmis.auth.service.AccessTokenService.ACCESS_TOKEN;
import static org.openlmis.auth.service.AccessTokenService.AUTHORIZATION;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class AccessTokenServiceTest {
  private static final String AUTHORIZATION_URL = "http://localhost/auth/oauth/token";

  private static final String CLIENT_ID = "client_id";
  private static final String CLIENT_SECRET = "client_secret";
  private static final String TOKEN = "token";

  private static final String URI_QUERY = "grant_type=client_credentials";

  @Mock
  private RestTemplate restTemplate;

  @Captor
  private ArgumentCaptor<URI> uriCaptor;

  @Captor
  private ArgumentCaptor<HttpEntity> entityCaptor;

  private AccessTokenService accessTokenService;

  @Before
  public void setUp() throws Exception {
    accessTokenService = new AccessTokenService();

    ReflectionTestUtils.setField(accessTokenService, "restTemplate", restTemplate);
    ReflectionTestUtils.setField(accessTokenService, "authorizationUrl", AUTHORIZATION_URL);
  }

  @Test
  public void shouldObtainToken() {
    // given
    Map<String, String> responseBody = ImmutableMap.of(ACCESS_TOKEN, TOKEN);
    ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

    // when
    given(restTemplate
        .exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
        .willReturn(response);

    String token = accessTokenService.obtainToken(CLIENT_ID, CLIENT_SECRET);

    // then
    assertThat(token, is(equalTo(TOKEN)));

    verify(restTemplate)
        .exchange(uriCaptor.capture(), eq(HttpMethod.POST), entityCaptor.capture(), eq(Map.class));

    URI uri = uriCaptor.getValue();
    assertThat(uri.toString(), startsWith(AUTHORIZATION_URL));
    assertThat(uri.getQuery(), containsString(URI_QUERY));

    HttpEntity entity = entityCaptor.getValue();
    assertThat(entity.getBody(), is(nullValue()));
    assertThat(entity.getHeaders(), hasKey(AUTHORIZATION));

    String plainCreds = CLIENT_ID + ":" + CLIENT_SECRET;
    byte[] plainCredsBytes = plainCreds.getBytes();
    byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
    String base64Creds = new String(base64CredsBytes);

    List<String> authorizations = entity.getHeaders().get(AUTHORIZATION);
    assertThat(authorizations, hasSize(1));
    assertThat(authorizations.get(0), equalTo("Basic " + base64Creds));

  }
}
