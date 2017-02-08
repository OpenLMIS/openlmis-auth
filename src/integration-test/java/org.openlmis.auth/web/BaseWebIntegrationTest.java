package org.openlmis.auth.web;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import guru.nidi.ramltester.junit.RamlMatchers;
import com.jayway.restassured.RestAssured;
import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.restassured.RestAssuredClient;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.openlmis.auth.Application;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest("server.port:8080")
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

  protected RamlDefinition ramlDefinition;
  protected RestAssuredClient restAssured;

  /** Prepare the test environment. */
  @Before
  public void setUp() {
    RestAssured.baseURI = BASE_URL;
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

  String getToken() {
    if (token == null) {
      token = fetchToken("admin", "password");
    }
    return token;
  }
}
