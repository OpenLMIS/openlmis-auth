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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_API_KEY_NOT_FOUND;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_CLIENT_NOT_FOUND;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_CLIENT_NOT_SUPPORTED;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_TOKEN_INVALID;
import static org.openlmis.auth.service.PermissionService.SERVICE_ACCOUNTS_MANAGE;
import static org.openlmis.auth.web.TestWebData.Fields.MESSAGE_KEY;
import static org.openlmis.auth.web.TestWebData.Tokens.API_KEY_TOKEN;
import static org.openlmis.auth.web.TestWebData.Tokens.SERVICE_TOKEN;
import static org.openlmis.auth.web.TestWebData.Tokens.USER_TOKEN;

import com.jayway.restassured.response.ValidatableResponse;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.auth.ApiKeyDataBuilder;
import org.openlmis.auth.ClientDataBuilder;
import org.openlmis.auth.DummyRightDto;
import org.openlmis.auth.DummyUserDto;
import org.openlmis.auth.OAuth2AuthenticationDataBuilder;
import org.openlmis.auth.domain.ApiKey;
import org.openlmis.auth.domain.Client;
import org.openlmis.auth.dto.ApiKeyDto;
import org.openlmis.auth.dto.ResultDto;
import org.openlmis.auth.dto.RightDto;
import org.openlmis.auth.dto.referencedata.UserDto;
import org.openlmis.auth.repository.ApiKeyRepository;
import org.openlmis.auth.repository.ClientRepository;
import org.openlmis.auth.service.AccessTokenService;
import org.openlmis.auth.service.consul.ConsulCommunicationService;
import org.openlmis.auth.util.AuthenticationHelper;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.provider.token.TokenStore;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Collections;
import java.util.Optional;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.UnusedPrivateField"})
public class ApiKeyControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String RESOURCE_URL = "/api/apiKeys";
  private static final String TOKEN_URL = RESOURCE_URL + "/{token}";

  private static final String TOKEN = "token";
  private static final String CLIENT_ID = "client_id";

  @MockBean
  private TokenStore tokenStore;

  @MockBean
  private AccessTokenService accessTokenService;

  @MockBean
  private ClientRepository clientRepository;

  @MockBean
  private ConsulCommunicationService consulCommunicationService;

  @MockBean
  private ApiKeyRepository apiKeyRepository;

  @MockBean
  private AuthenticationHelper authenticationHelper;

  private Client client = new ClientDataBuilder().buildUserClient();

  private ApiKey key;

  private UserDto user = new DummyUserDto();
  private RightDto right = new DummyRightDto();

  @Before
  @Override
  public void setUp() {
    super.setUp();

    client = new Client();
    key = new ApiKeyDataBuilder().build();

    given(clientRepository.findOneByClientId(CLIENT_ID)).willReturn(Optional.of(client));
    given(clientRepository.saveAndFlush(any(Client.class))).willReturn(client);

    given(tokenStore.readAuthentication(key.getToken().toString()))
        .willReturn(new OAuth2AuthenticationDataBuilder().withClientId(CLIENT_ID).build());

    given(accessTokenService.obtainToken(anyString()))
        .willReturn(key.getToken());

    given(apiKeyRepository.findOne(key.getToken())).willReturn(key);
    given(apiKeyRepository.save(any(ApiKey.class)))
        .willAnswer(invocation -> invocation.getArguments()[0]);

    given(authenticationHelper.getCurrentUser()).willReturn(user);
    given(authenticationHelper.getRight(SERVICE_ACCOUNTS_MANAGE)).willReturn(right);

    given(userReferenceDataService.hasRight(user.getId(), right.getId(), null, null,null))
        .willReturn(new ResultDto<>(true));

  }

  @Test
  public void shouldCreateApiKey() {
    ApiKeyDto response = post(USER_TOKEN)
        .statusCode(HttpStatus.CREATED.value())
        .extract()
        .as(ApiKeyDto.class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    assertThat(response.getToken(), is(equalTo(key.getToken())));
    assertThat(response.getCreatedBy(), is(equalTo(user.getId())));
    assertThat(response.getCreatedDate(), is(notNullValue()));
  }

  @Test
  public void shouldReturnForbiddenForCreateApiKeyEndpointWhenUserHasNoRight() {
    given(userReferenceDataService.hasRight(user.getId(), right.getId(), null, null,null))
        .willReturn(new ResultDto<>(false));

    String response = post(USER_TOKEN)
        .statusCode(HttpStatus.FORBIDDEN.value())
        .extract()
        .path(MESSAGE_KEY);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertThat(response, is(equalTo(ERROR_NO_FOLLOWING_PERMISSION)));
  }

  @Test
  public void shouldReturnUnauthorizedWithoutAuthorizationForCreateApiKeyEndpoint() {
    restAssured
        .given()
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotAllowToCreateApiKeyByAnotherApiKey() {
    post(API_KEY_TOKEN)
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .body(MESSAGE_KEY, equalTo(ERROR_CLIENT_NOT_SUPPORTED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotAllowToCreateApiKeyByService() {
    post(SERVICE_TOKEN)
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .body(MESSAGE_KEY, equalTo(ERROR_CLIENT_NOT_SUPPORTED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRetrieveApiKeys() {
    given(apiKeyRepository.findAll(any(Pageable.class)))
        .willReturn(new PageImpl<>(Lists.newArrayList(key, key, key)));

    ValidatableResponse response = get(10, USER_TOKEN).statusCode(HttpStatus.OK.value());
    checkPageBody(response, 0, 10, 3, 3, 1);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRetrieveOnePageOfApiKeys() {
    given(apiKeyRepository.findAll(any(Pageable.class)))
        .willReturn(new PageImpl<>(Collections.singletonList(key), null, 3));

    ValidatableResponse response = get(1, 1, USER_TOKEN).statusCode(HttpStatus.OK.value());
    checkPageBody(response, 1, 1, 1, 3, 3);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenForGetApiKeysEndpointWhenUserHasNoRight() {
    given(userReferenceDataService.hasRight(user.getId(), right.getId(), null, null,null))
        .willReturn(new ResultDto<>(false));

    String response = get(10, USER_TOKEN)
        .statusCode(HttpStatus.FORBIDDEN.value())
        .extract()
        .path(MESSAGE_KEY);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertThat(response, is(equalTo(ERROR_NO_FOLLOWING_PERMISSION)));
  }

  @Test
  public void shouldReturnUnauthorizedWithoutAuthorizationForGetApiKeysEndpoint() {
    restAssured
        .given()
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotAllowToRetrieveApiKeysByAnotherApiKey() {
    get(10, API_KEY_TOKEN)
        .statusCode(HttpStatus.FORBIDDEN.value())
        .body(MESSAGE_KEY, equalTo(ERROR_NO_FOLLOWING_PERMISSION));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldAllowToRetrieveApiKeysKeyByService() {
    given(apiKeyRepository.findAll(any(Pageable.class)))
        .willReturn(new PageImpl<>(Collections.emptyList()));

    get(10, SERVICE_TOKEN).statusCode(HttpStatus.OK.value());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteApiKey() {
    delete(USER_TOKEN).statusCode(HttpStatus.NO_CONTENT.value());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenForDeleteApiKeyEndpointWhenUserHasNoRight() {
    given(userReferenceDataService.hasRight(user.getId(), right.getId(), null, null,null))
        .willReturn(new ResultDto<>(false));

    String response = delete(USER_TOKEN)
        .statusCode(HttpStatus.FORBIDDEN.value())
        .extract()
        .path(MESSAGE_KEY);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertThat(response, is(equalTo(ERROR_NO_FOLLOWING_PERMISSION)));
  }

  @Test
  public void shouldReturnNotFoundIfKeyNotExistForDeleteApiKeyEndpoint() {
    given(apiKeyRepository.findOne(key.getToken()))
        .willReturn(null);

    String response = delete(USER_TOKEN)
        .statusCode(HttpStatus.NOT_FOUND.value())
        .extract()
        .path(MESSAGE_KEY);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertThat(response, is(equalTo(ERROR_API_KEY_NOT_FOUND)));
  }

  @Test
  public void shouldNotAllowToRemoveKeyIfThereIsNoAuthentication() {
    given(tokenStore.readAuthentication(key.getToken().toString()))
        .willReturn(null);

    delete(USER_TOKEN)
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .body(MESSAGE_KEY, equalTo(ERROR_TOKEN_INVALID));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotAllowToRemoveKeyIfThereIsNoClient() {
    given(clientRepository.findOneByClientId(CLIENT_ID))
        .willReturn(Optional.empty());

    delete(USER_TOKEN)
        .statusCode(HttpStatus.NOT_FOUND.value())
        .body(MESSAGE_KEY, equalTo(ERROR_CLIENT_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotAllowToRemoveApiKeyByAnotherApiKey() {
    delete(API_KEY_TOKEN)
        .statusCode(HttpStatus.FORBIDDEN.value())
        .body(MESSAGE_KEY, equalTo(ERROR_NO_FOLLOWING_PERMISSION));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldAllowToRemoveApiKeyByService() {
    delete(SERVICE_TOKEN)
        .statusCode(HttpStatus.NO_CONTENT.value());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedWithoutAuthorizationForDeleteApiKeyEndpoint() {
    restAssured
        .given()
        .pathParam(TOKEN, key.getToken())
        .when()
        .delete(TOKEN_URL)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private ValidatableResponse post(String token) {
    return sendPostRequest(token, RESOURCE_URL, null, null);
  }

  private ValidatableResponse get(int pageSize, String token) {
    return get(pageSize, 0, token);
  }

  private ValidatableResponse get(int pageSize, int page, String token) {
    return startRequest(token)
        .queryParam("page", page)
        .queryParam("size", pageSize)
        .when()
        .get(RESOURCE_URL)
        .then();
  }

  private ValidatableResponse delete(String token) {
    return startRequest(token)
        .pathParam(TOKEN, key.getToken())
        .when()
        .delete(TOKEN_URL)
        .then();
  }
}
