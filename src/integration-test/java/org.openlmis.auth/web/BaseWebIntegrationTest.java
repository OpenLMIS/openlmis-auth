package org.openlmis.auth.web;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.codec.binary.Base64;
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest("server.port:8080")
public abstract class BaseWebIntegrationTest {
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
    RestTemplate restTemplate = new RestTemplate();

    String plainCreds = "trusted-client:secret";
    byte[] plainCredsBytes = plainCreds.getBytes();
    byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
    String base64Creds = new String(base64CredsBytes);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Basic " + base64Creds);

    HttpEntity<String> request = new HttpEntity<>(headers);
    ResponseEntity<?> response = restTemplate.exchange(
        BASE_URL + "/api/oauth/token?grant_type=password&username="
            + username + "&password=" + password,
        HttpMethod.POST, request, Object.class);

    return ((Map<String, String>) response.getBody()).get("access_token");
  }

  String getToken(String username, String password) {
    token = fetchToken(username, password);
    return token;
  }

  String getToken() {
    if (token == null) {
      token = fetchToken("admin", "password");
    }
    return token;
  }

  String addTokenToUrl(String url) {
    return url + "?access_token=" + this.getToken();
  }
}
