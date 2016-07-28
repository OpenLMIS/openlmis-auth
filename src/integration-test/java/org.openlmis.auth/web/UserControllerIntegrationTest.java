package org.openlmis.auth.web;

import com.jayway.restassured.RestAssured;
import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.junit.RamlMatchers;
import guru.nidi.ramltester.restassured.RestAssuredClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.i18n.ExposedMessageSource;
import org.openlmis.auth.util.PasswordResetRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;

public class UserControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String RAML_ASSERT_MESSAGE = "HTTP request/response should match RAML "
      + "definition.";
  private static final String USER_ID = "51f6bdc1-4932-4bc3-9589-368646ef7ad3";
  private static final String USERNAME = "admin";

  private RamlDefinition ramlDefinition;
  private RestAssuredClient restAssured;

  @Autowired
  private ExposedMessageSource messageSource;

  /** Prepare the test environment. */
  @Before
  public void setUp() {
    RestAssured.baseURI = BASE_URL;
    ramlDefinition = RamlLoaders.fromClasspath().load("api-definition-raml.yaml");
    restAssured = ramlDefinition.createRestAssured();
  }

  @Test
  public void testPasswordReset() {
    User user = restAssured.given()
        .queryParam("access_token", getToken())
        .when()
        .get("/api/users/" + USER_ID)
        .then()
        .statusCode(200)
        .extract().as(User.class);
    String password = user.getPassword();
    Assert.assertNotNull(password);

    PasswordResetRequest passwordResetRequest = new PasswordResetRequest(USERNAME, "test");
    String response = restAssured.given()
        .contentType("application/json")
        .content(passwordResetRequest)
        .when()
        .post("/api/users/passwordReset")
        .then()
        .statusCode(200)
        .extract().asString();

    String[] msgArgs = {USERNAME};
    Assert.assertTrue(response.contains(messageSource.getMessage(
        "users.passwordReset.confirmation", msgArgs, LocaleContextHolder.getLocale())));
    Assert.assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(),
        RamlMatchers.hasNoViolations());

    user = restAssured.given()
        .queryParam("access_token", getToken(USERNAME, "test"))
        .when()
        .get("/api/users/" + USER_ID)
        .then()
        .statusCode(200)
        .extract().as(User.class);
    String newPassword = user.getPassword();
    Assert.assertNotNull(newPassword);

    Assert.assertNotEquals(password, newPassword);
  }
}
