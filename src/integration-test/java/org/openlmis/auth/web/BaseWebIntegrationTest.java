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

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.openlmis.auth.web.TestWebData.Tokens.API_KEY_TOKEN;
import static org.openlmis.auth.web.TestWebData.Tokens.BEARER;
import static org.openlmis.auth.web.TestWebData.Tokens.SERVICE_TOKEN;
import static org.openlmis.auth.web.TestWebData.Tokens.USER_TOKEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.ObjectMapperConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.filter.log.LogDetail;
import com.jayway.restassured.response.ValidatableResponse;
import com.jayway.restassured.specification.RequestSpecification;
import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.restassured.RestAssuredClient;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.junit.runner.RunWith;
import org.openlmis.auth.ClientDataBuilder;
import org.openlmis.auth.OAuth2AuthenticationDataBuilder;
import org.openlmis.auth.domain.Client;
import org.openlmis.auth.i18n.MessageService;
import org.openlmis.auth.repository.ApiKeyRepository;
import org.openlmis.auth.repository.ClientRepository;
import org.openlmis.auth.security.AccessTokenEnhancer;
import org.openlmis.auth.service.referencedata.UserReferenceDataService;
import org.openlmis.auth.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.main.allow-bean-definition-overriding=true")
@ActiveProfiles({"test", "test-run"})
@DirtiesContext
public abstract class BaseWebIntegrationTest {
  private static final String BASE_URL = System.getenv("BASE_URL");

  static final String RAML_ASSERT_MESSAGE = "HTTP request/response should match RAML definition.";

  RestAssuredClient restAssured;

  private static final RamlDefinition ramlDefinition =
      RamlLoaders.fromClasspath().load("api-definition-raml.yaml").ignoringXheaders();

  static final String ID = "id";

  @Autowired
  private MessageService messageService;

  @LocalServerPort
  private int randomPort;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  UserReferenceDataService userReferenceDataService;

  @MockBean
  ApiKeyRepository apiKeyRepository;

  @MockBean
  ClientRepository clientRepository;

  /**
   * Initialize the REST Assured client. Done here and not in the constructor, so that randomPort is
   * available.
   */
  @PostConstruct
  public void init() {
    RestAssured.baseURI = BASE_URL;
    RestAssured.port = randomPort;
    RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
        new ObjectMapperConfig().jackson2ObjectMapperFactory((clazz, charset) -> objectMapper)
    );
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    restAssured = ramlDefinition.createRestAssured();
  }

  String getMessage(Message message) {
    return messageService.localize(message).asMessage();
  }

  ValidatableResponse sendPostRequest(String token, String url, Object content,
                                      Map<String, Object> query) {
    RequestSpecification request = startRequest(token);

    if (null != query) {
      for (Map.Entry<String, Object> entry : query.entrySet()) {
        request = request.queryParam(entry.getKey(), entry.getValue());
      }
    }

    if (null != content) {
      request = request.content(content);
    }

    return request
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .post(url)
        .then();
  }

  RequestSpecification startRequest() {
    return restAssured.given().log().ifValidationFails(LogDetail.ALL, true);
  }

  RequestSpecification startRequest(String token) {
    RequestSpecification request = startRequest();

    if (isNotBlank(token)) {
      request = request.header(HttpHeaders.AUTHORIZATION, BEARER + token);
    }

    return request;
  }

  void checkPageBody(ValidatableResponse response, int page, int size, int numberOfElements,
                     int totalElements, int totalPages) {
    response
        .body("number", is(page))
        .body("size", is(size))
        .body("numberOfElements", is(numberOfElements))
        .body("content.size()", is(numberOfElements))
        .body("totalElements", is(totalElements))
        .body("totalPages", is(totalPages));
  }

  Client mockUserClient() {
    Client client = new ClientDataBuilder().buildUserClient();
    given(clientRepository.findOneByClientId(client.getClientId()))
        .willReturn(Optional.of(client));

    return client;
  }

  @TestConfiguration
  static class TestConfig {

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    @Qualifier("clientDetailsServiceImpl")
    private ClientDetailsService clientDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Value("${token.validitySeconds}")
    private Integer tokenValiditySeconds;

    @Bean
    public DefaultTokenServices defaultTokenServices() {
      DefaultTokenServices tokenServices = new TestTokenServices();
      tokenServices.setTokenStore(tokenStore);
      tokenServices.setSupportRefreshToken(true);
      tokenServices.setClientDetailsService(clientDetailsService);
      tokenServices.setTokenEnhancer(new AccessTokenEnhancer());
      tokenServices.setAccessTokenValiditySeconds(tokenValiditySeconds);
      tokenServices.setAuthenticationManager(authenticationManager);

      return tokenServices;
    }

    private static class TestTokenServices extends DefaultTokenServices {

      @Override
      public OAuth2Authentication loadAuthentication(String accessTokenValue) {
        switch (accessTokenValue) {
          case USER_TOKEN:
            return new OAuth2AuthenticationDataBuilder().buildUserAuthentication();
          case SERVICE_TOKEN:
            return new OAuth2AuthenticationDataBuilder().buildServiceAuthentication();
          case API_KEY_TOKEN:
            return new OAuth2AuthenticationDataBuilder().buildApiKeyAuthentication();
          default:
            return super.loadAuthentication(accessTokenValue);
        }
      }
    }

  }

}
