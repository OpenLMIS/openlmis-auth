package org.openlmis.auth.web;

import guru.nidi.ramltester.junit.RamlMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.openlmis.auth.domain.PasswordResetToken;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.i18n.ExposedMessageSource;
import org.openlmis.auth.repository.PasswordResetTokenRepository;
import org.openlmis.auth.repository.UserRepository;
import org.openlmis.auth.util.PasswordChangeRequest;
import org.openlmis.util.PasswordResetRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

public class UserControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String USER_ID = "51f6bdc1-4932-4bc3-9589-368646ef7ad3";
  private static final String USERNAME = "admin";
  private static final String EMAIL = "test@openlmis.org";
  private static final String REFERENCE_DATA_USER_ID = "35316636-6264-6331-2d34-3933322d3462";
  private static final String PASSWORD = "password";

  @Autowired
  private ExposedMessageSource messageSource;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @After
  public void cleanUp() {
    passwordResetTokenRepository.deleteAll();
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    User user = userRepository.findOne(UUID.fromString(USER_ID));
    user.setPassword(encoder.encode(PASSWORD));
    userRepository.save(user);
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
        .post("/api/users/auth/passwordReset")
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

    String newPassword = getPassword();
    Assert.assertNotNull(newPassword);
    Assert.assertNotEquals(password, newPassword);

    testChangePassword("1234567", "size must be between 8 and 16");
    testChangePassword("sdokfsodpfjsaidjasj2akdsjk", "size must be between 8 and 16");
    testChangePassword("vvvvvvvvvvv", "must contain at least 1 number");
    testChangePassword("1sample text", "must not contain spaces");
  }

  private String logoutUser(Integer statusCode, String token) {
    return restAssured.given()
        .queryParam("access_token", token)
        .when()
        .post("/api/users/auth/logout")
        .then()
        .statusCode(statusCode)
        .extract().asString();
  }

  @Test
  public void revokeTokenTest() {
    String accessToken = getToken();
    String response = logoutUser(200, accessToken);

    Assert.assertTrue(response.contains("You have successfully logged out!"));

    logoutUser(401, accessToken);
  }

  @Test
  public void testForgotPassword() {
    restAssured.given()
        .queryParam("email", EMAIL)
        .when()
        .post("/api/users/auth/forgotPassword")
        .then()
        .statusCode(200);

    User user = userRepository.findOne(UUID.fromString(USER_ID));
    Assert.assertNotNull(user);

    PasswordResetToken token = passwordResetTokenRepository.findOneByUser(user);
    Assert.assertNotNull(token);

    PasswordChangeRequest request = new PasswordChangeRequest(token.getId(), "test");
    restAssured.given()
        .contentType("application/json")
        .content(request)
        .when()
        .post("/api/users/auth/changePassword")
        .then()
        .statusCode(200);

    User changedUser = userRepository.findOne(UUID.fromString(USER_ID));
    Assert.assertNotNull(changedUser);
    Assert.assertNotEquals(changedUser.getPassword(), user.getPassword());
  }

  @Test
  public void testCreatePasswordResetToken() {
    UUID tokenId = restAssured.given()
        .queryParam("access_token", getToken())
        .queryParam("userId", REFERENCE_DATA_USER_ID)
        .when()
        .post("/api/users/auth/passwordResetToken")
        .then()
        .statusCode(200)
        .extract().as(UUID.class);

    PasswordResetToken token = passwordResetTokenRepository.findOne(tokenId);
    Assert.assertNotNull(token);
  }
}
