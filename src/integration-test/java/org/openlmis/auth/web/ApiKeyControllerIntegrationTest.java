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

package org.openlmis.auth.web;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.verifyZeroInteractions;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_CLIENT_NOT_FOUND;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_TOKEN_INVALID;
import static org.openlmis.auth.web.TestWebData.Tokens.API_KEY_TOKEN;
import static org.openlmis.auth.web.TestWebData.Tokens.SERVICE_TOKEN;
import static org.openlmis.auth.web.TestWebData.Tokens.USER_TOKEN;

import com.jayway.restassured.response.ValidatableResponse;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.auth.OAuth2AuthenticationDataBuilder;
import org.openlmis.auth.domain.Client;
import org.openlmis.auth.repository.ClientRepository;
import org.openlmis.auth.service.AccessTokenService;
import org.openlmis.auth.service.consul.ConsulCommunicationService;
import org.openlmis.auth.web.TestWebData.Fields;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Optional;

@SuppressWarnings("PMD.TooManyMethods")
public class ApiKeyControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String RESOURCE_URL = "/api/apiKeys";
  private static final String KEY_URL = RESOURCE_URL + "/{key}";

  private static final String KEY = "key";

  private static final String NEW_API_KEY_TOKEN = "api-key-token";
  private static final String CLIENT_ID = "client_id";

  @MockBean
  private TokenStore tokenStore;

  @MockBean
  private AccessTokenService accessTokenService;

  @MockBean
  private ClientRepository clientRepository;

  @MockBean
  private ConsulCommunicationService consulCommunicationService;

  private Client client = new Client();

  @Before
  @Override
  public void setUp() {
    super.setUp();

    client = new Client();
    given(clientRepository.findOneByClientId(CLIENT_ID)).willReturn(Optional.of(client));
    given(clientRepository.saveAndFlush(any(Client.class))).willReturn(client);

    given(tokenStore.readAuthentication(NEW_API_KEY_TOKEN))
        .willReturn(new OAuth2AuthenticationDataBuilder().withClientId(CLIENT_ID).build());

    given(accessTokenService.obtainToken(anyString())).willReturn(NEW_API_KEY_TOKEN);
  }

  @Test
  public void shouldCreateApiKey() {
    post(SERVICE_TOKEN)
        .statusCode(HttpStatus.CREATED.value())
        .body(equalTo(NEW_API_KEY_TOKEN));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotAllowToCreateApiKeyIfUnauthorized() {
    post(null)
        .statusCode(HttpStatus.UNAUTHORIZED.value());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(clientRepository, accessTokenService, consulCommunicationService);
  }

  @Test
  public void shouldNotAllowToCreateApiKeyByAnotherApiKey() {
    post(API_KEY_TOKEN)
        .statusCode(HttpStatus.FORBIDDEN.value())
        .body(Fields.MESSAGE_KEY, equalTo(ERROR_NO_FOLLOWING_PERMISSION));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(clientRepository, accessTokenService, consulCommunicationService);
  }

  @Test
  public void shouldNotAllowToCreateApiKeyByUser() {
    post(USER_TOKEN)
        .statusCode(HttpStatus.FORBIDDEN.value())
        .body(Fields.MESSAGE_KEY, equalTo(ERROR_NO_FOLLOWING_PERMISSION));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(clientRepository, accessTokenService, consulCommunicationService);
  }

  @Test
  public void shouldRemoveApiKey() {
    delete(SERVICE_TOKEN)
        .statusCode(HttpStatus.NO_CONTENT.value());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    verify(tokenStore).readAuthentication(NEW_API_KEY_TOKEN);
    verify(tokenStore).removeAccessToken(new DefaultOAuth2AccessToken(NEW_API_KEY_TOKEN));

    verify(clientRepository).findOneByClientId(CLIENT_ID);
    verify(clientRepository).delete(client);
  }

  @Test
  public void shouldNotAllowToRemoveApiKeyIfUnauthorized() {
    delete(null)
        .statusCode(HttpStatus.UNAUTHORIZED.value());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(clientRepository, accessTokenService);
  }

  @Test
  public void shouldNotAllowToRemoveApiKeyByAnotherApiKey() {
    delete(API_KEY_TOKEN)
        .statusCode(HttpStatus.FORBIDDEN.value())
        .body(Fields.MESSAGE_KEY, equalTo(ERROR_NO_FOLLOWING_PERMISSION));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(clientRepository, tokenStore);
  }

  @Test
  public void shouldNotAllowToRemoveApiKeyByUser() {
    delete(USER_TOKEN)
        .statusCode(HttpStatus.FORBIDDEN.value())
        .body(Fields.MESSAGE_KEY, equalTo(ERROR_NO_FOLLOWING_PERMISSION));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(clientRepository, tokenStore);
  }

  @Test
  public void shouldNotAllowToRemoveKeyIfThereIsNoAuthentication() {
    given(tokenStore.readAuthentication(NEW_API_KEY_TOKEN))
        .willReturn(null);

    delete(SERVICE_TOKEN)
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .body(Fields.MESSAGE_KEY, equalTo(ERROR_TOKEN_INVALID));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    verify(tokenStore).readAuthentication(NEW_API_KEY_TOKEN);
    verify(tokenStore, never()).removeAccessToken(any(OAuth2AccessToken.class));

    verifyZeroInteractions(clientRepository);
  }

  @Test
  public void shouldNotAllowToRemoveKeyIfThereIsNoClient() {
    given(clientRepository.findOneByClientId(CLIENT_ID))
        .willReturn(Optional.empty());

    delete(SERVICE_TOKEN)
        .statusCode(HttpStatus.NOT_FOUND.value())
        .body(Fields.MESSAGE_KEY, equalTo(ERROR_CLIENT_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    verify(tokenStore).readAuthentication(NEW_API_KEY_TOKEN);
    verify(tokenStore, never()).removeAccessToken(any(OAuth2AccessToken.class));

    verify(clientRepository).findOneByClientId(CLIENT_ID);
    verify(clientRepository, never()).delete(any(Client.class));
  }

  private ValidatableResponse post(String token) {
    return sendPostRequest(token, RESOURCE_URL, null, null);
  }

  private ValidatableResponse delete(String token) {
    return startRequest(token)
        .pathParam(KEY, NEW_API_KEY_TOKEN)
        .when()
        .delete(KEY_URL)
        .then();
  }

}
