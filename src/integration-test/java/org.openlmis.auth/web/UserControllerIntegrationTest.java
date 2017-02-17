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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openlmis.auth.web.UserController.RESET_PASSWORD_TOKEN_VALIDITY_HOURS;

import org.junit.After;
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

import java.time.ZonedDateTime;
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
    assertTrue(response.contains(expectedMessage));
  }

  @Test
  public void testPasswordReset() {
    String password = getPassword();
    assertNotNull(password);

    String[] msgArgs = {USERNAME};
    String expectedMessage = messageSource.getMessage("users.passwordReset.confirmation",
        msgArgs, LocaleContextHolder.getLocale());

    testChangePassword("test1234", expectedMessage);

    String newPassword = getPassword();
    assertNotNull(newPassword);
    assertNotEquals(password, newPassword);

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

    assertTrue(response.contains("You have successfully logged out!"));

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
    assertNotNull(user);

    PasswordResetToken token = passwordResetTokenRepository.findOneByUser(user);
    assertNotNull(token);

    PasswordChangeRequest request = new PasswordChangeRequest(token.getId(), "test");
    restAssured.given()
        .contentType("application/json")
        .content(request)
        .when()
        .post("/api/users/auth/changePassword")
        .then()
        .statusCode(200);

    User changedUser = userRepository.findOne(UUID.fromString(USER_ID));
    assertNotNull(changedUser);
    assertNotEquals(changedUser.getPassword(), user.getPassword());
  }

  @Test
  public void shouldCreateNewTokenAfterEachForgotPasswordRequest() {
    User user1 = userRepository.findOne(UUID.fromString(USER_ID));
    assertNotNull(user1);

    PasswordResetToken token1 = new PasswordResetToken();
    token1.setUser(user1);
    token1.setExpiryDate(ZonedDateTime.now().plusHours(RESET_PASSWORD_TOKEN_VALIDITY_HOURS));

    passwordResetTokenRepository.save(token1);

    restAssured.given()
        .queryParam("email", EMAIL)
        .when()
        .post("/api/users/auth/forgotPassword")
        .then()
        .statusCode(200);

    User user2 = userRepository.findOne(UUID.fromString(USER_ID));
    assertNotNull(user2);
    assertEquals(user1.getId(), user2.getId());

    PasswordResetToken token2 = passwordResetTokenRepository.findOneByUser(user2);
    assertNotNull(token2);
    assertNotEquals(token1.getId(), token2.getId());
  }

  @Test
  public void testForgotPasswordRollback() {
    wireMockRule.stubFor(post(urlPathEqualTo("/api/notification"))
        .willReturn(aResponse()
            .withStatus(400)));

    restAssured.given()
        .queryParam("email", EMAIL)
        .when()
        .post("/api/users/auth/forgotPassword")
        .then()
        .statusCode(500);

    User user = userRepository.findOne(UUID.fromString(USER_ID));
    assertNotNull(user);

    PasswordResetToken token = passwordResetTokenRepository.findOneByUser(user);
    assertNull(token);
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
    assertNotNull(token);
  }
}
