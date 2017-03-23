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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.RestAssured;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.junit.RamlMatchers;
import guru.nidi.ramltester.restassured.RestAssuredClient;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext
public abstract class BaseWebIntegrationTest {
  private static final String RAML_ASSERT_MESSAGE = "HTTP request/response should match RAML "
      + "definition.";
  static final String BASE_URL = System.getenv("BASE_URL");

  private String token = null;

  private static final String MOCK_TOKEN_REQUEST_RESPONSE = "{"
          + "  \"access_token\": \"418c89c5-7f21-4cd1-a63a-38c47892b0fe\",\n"
          + "  \"token_type\": \"bearer\",\n"
          + "  \"expires_in\": 847,\n"
          + "  \"scope\": \"read write\",\n"
          + "  \"referenceDataUserId\": \"35316636-6264-6331-2d34-3933322d3462\"\n"
          + "}";

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(80);

  @LocalServerPort
  private int randomPort;

  protected RamlDefinition ramlDefinition;
  protected RestAssuredClient restAssured;

  /** Prepare the test environment. */
  @Before
  public void setUp() {
    RestAssured.baseURI = BASE_URL;
    RestAssured.port = randomPort;
    ramlDefinition = RamlLoaders.fromClasspath().load("api-definition-raml.yaml")
        .ignoringXheaders();
    restAssured = ramlDefinition.createRestAssured();
  }

  /**
   * Constructor for test.
   */
  public BaseWebIntegrationTest() {
    // This mocks the auth token request response
    wireMockRule.stubFor(post(urlPathEqualTo("/api/oauth/token?grant_type=client_credentials"))
            .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(MOCK_TOKEN_REQUEST_RESPONSE)));

    // This mocks the call to notification to post a notification.
    wireMockRule.stubFor(post(urlPathEqualTo("/api/notification"))
            .willReturn(aResponse()
                    .withStatus(200)));
  }

  String getToken() {
    if (token == null) {
      token = fetchToken("admin", "password");
    }
    return token;
  }

  private String fetchToken(String username, String password) {
    String token = restAssured.given()
        .auth().preemptive().basic("user-client", "changeme")
        .queryParam("grant_type", "password")
        .queryParam("username", username)
        .queryParam("password", password)
        .when()
        .post("/api/oauth/token")
        .then()
        .extract()
        .path("access_token");
    Assert.assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(),
        RamlMatchers.hasNoViolations());
    return token;
  }
}
