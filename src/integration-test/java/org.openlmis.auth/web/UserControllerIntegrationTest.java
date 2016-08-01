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

  private String getPassword() {
    User user = restAssured.given()
        .queryParam("access_token", getToken())
        .when()
        .get("/api/users/" + USER_ID)
        .then()
        .statusCode(200)
        .extract().as(User.class);
    return user.getPassword();
  }

  private String changePassword(String password) {
    PasswordResetRequest passwordResetRequest = new PasswordResetRequest(USERNAME, password);
    return restAssured.given()
        .contentType("application/json")
        .content(passwordResetRequest)
        .when()
        .post("/api/users/passwordReset")
        .then()
        .extract().asString();
  }

  private void testChangePassword(String password, String expectedMessage) {
    String response = changePassword(password);
    Assert.assertTrue(response.contains(expectedMessage));
  }

  @Test
  public void testPasswordReset() {
    String password = getPassword();
    Assert.assertNotNull(password);

    String[] msgArgs = {USERNAME};
    String expectedMessage = messageSource.getMessage("users.passwordReset.confirmation",
        msgArgs, LocaleContextHolder.getLocale());

    testChangePassword("test1234", expectedMessage);
    Assert.assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(),
        RamlMatchers.hasNoViolations());

    String newPassword = getPassword();
    Assert.assertNotNull(newPassword);
    Assert.assertNotEquals(password, newPassword);

    testChangePassword("1234567", "size must be between 8 and 16");
    testChangePassword("sdokfsodpfjsaidjasj2akdsjk", "size must be between 8 and 16");
    testChangePassword("vvvvvvvvvvv", "must contain at least 1 number");
    testChangePassword("1sample text", "must not contain spaces");
  }
}
