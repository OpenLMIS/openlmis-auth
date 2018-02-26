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

import static com.google.common.collect.ImmutableMap.of;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;
import static org.openlmis.auth.service.UserService.RESET_PASSWORD_TOKEN_VALIDITY_HOURS;
import static org.openlmis.auth.web.TestWebData.Tokens.USER_TOKEN;

import com.jayway.restassured.response.ValidatableResponse;
import guru.nidi.ramltester.junit.RamlMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.auth.DummyUserDto;
import org.openlmis.auth.domain.PasswordResetToken;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.exception.PermissionMessageException;
import org.openlmis.auth.repository.PasswordResetTokenRepository;
import org.openlmis.auth.repository.UserRepository;
import org.openlmis.auth.service.PermissionService;
import org.openlmis.auth.service.notification.NotificationService;
import org.openlmis.auth.util.Message;
import org.openlmis.auth.util.PasswordChangeRequest;
import org.openlmis.auth.web.TestWebData.Fields;
import org.openlmis.auth.web.TestWebData.GrantTypes;
import org.openlmis.util.NotificationRequest;
import org.openlmis.util.PasswordResetRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.HttpServerErrorException;
import java.time.ZonedDateTime;
import java.util.UUID;

@SuppressWarnings("PMD.TooManyMethods")
public class UserControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String RESOURCE_URL = "/api/users/auth";
  private static final String RESET_PASS_URL = RESOURCE_URL + "/passwordReset";
  private static final String FORGOT_PASS_URL = RESOURCE_URL + "/forgotPassword";
  private static final String CHANGE_PASS_URL = RESOURCE_URL + "/changePassword";
  private static final String RESET_TOKEN_PASS_URL = RESOURCE_URL + "/passwordResetToken";
  private static final String LOGOUT_URL = RESOURCE_URL + "/logout";

  private static final String TOKEN_URL = "/api/oauth/token";

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @SpyBean
  private PermissionService permissionService;

  @MockBean
  private NotificationService notificationService;

  @Override
  @Before
  public void setUp() {
    super.setUp();

    DummyUserDto admin = new DummyUserDto();

    given(userReferenceDataService.findUserByEmail(admin.getEmail()))
        .willReturn(admin);
    given(userReferenceDataService.findOne(admin.getId()))
        .willReturn(admin);

    willDoNothing().given(notificationService).send(any(NotificationRequest.class));
  }

  @After
  public void cleanUp() {
    passwordResetTokenRepository.deleteAll();
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    User user = userRepository.findOne(UUID.fromString(DummyUserDto.AUTH_ID));
    user.setPassword(encoder.encode(DummyUserDto.PASSWORD));
    userRepository.save(user);
  }

  @Test
  public void shouldNotSaveUserWhenUserHasNoPermission() {
    PermissionMessageException ex = mockUserManagePermissionError();

    sendPostRequest(USER_TOKEN, RESOURCE_URL, new User(), null)
        .statusCode(403)
        .body(Fields.MESSAGE, equalTo(getMessage(ex.asMessage())));
  }

  @Test
  public void testPasswordReset() {
    doNothing().when(permissionService).canManageUsers();

    String password = userRepository
        .findOne(UUID.fromString(DummyUserDto.AUTH_ID))
        .getPassword();
    assertNotNull(password);

    passwordReset("test1234", USER_TOKEN).statusCode(200);

    String newPassword = userRepository
        .findOne(UUID.fromString(DummyUserDto.AUTH_ID))
        .getPassword();
    assertNotNull(newPassword);
    assertNotEquals(password, newPassword);

    testChangePassword("1234567", "size must be between 8 and 16", 400);
    testChangePassword("sdokfsodpfjsaidjasj2akdsjk", "size must be between 8 and 16", 400);
    testChangePassword("vvvvvvvvvvv", "must contain at least 1 number", 400);
    testChangePassword("1sample text", "must not contain spaces", 400);
  }

  @Test
  public void shouldNotResetPasswordWhenUserHasNoPermission() {
    PermissionMessageException ex = mockUserManagePermissionError();

    passwordReset("newpassword", USER_TOKEN)
        .statusCode(403)
        .body(Fields.MESSAGE, equalTo(getMessage(ex.asMessage())));
  }

  @Test
  public void revokeTokenTest() {
    String accessToken = login(DummyUserDto.USERNAME, DummyUserDto.PASSWORD);
    String response = logoutUser(200, accessToken);

    assertTrue(response.contains("You have successfully logged out!"));

    logoutUser(401, accessToken);
  }

  @Test
  public void testForgotPassword() {
    forgotPassword().statusCode(200);

    User user = userRepository.findOne(UUID.fromString(DummyUserDto.AUTH_ID));
    assertNotNull(user);

    PasswordResetToken token = passwordResetTokenRepository.findOneByUser(user);
    assertNotNull(token);

    PasswordChangeRequest request = new PasswordChangeRequest(token.getId(), "test");
    changePassword(request).statusCode(200);

    User changedUser = userRepository.findOne(UUID.fromString(DummyUserDto.AUTH_ID));
    assertNotNull(changedUser);
    assertNotEquals(changedUser.getPassword(), user.getPassword());
  }

  @Test
  public void shouldCreateNewTokenAfterEachForgotPasswordRequest() {
    User user1 = userRepository.findOne(UUID.fromString(DummyUserDto.AUTH_ID));
    assertNotNull(user1);

    PasswordResetToken token1 = new PasswordResetToken();
    token1.setUser(user1);
    token1.setExpiryDate(ZonedDateTime.now().plusHours(RESET_PASSWORD_TOKEN_VALIDITY_HOURS));

    passwordResetTokenRepository.save(token1);

    forgotPassword().statusCode(200);

    User user2 = userRepository.findOne(UUID.fromString(DummyUserDto.AUTH_ID));
    assertNotNull(user2);
    assertEquals(user1.getId(), user2.getId());

    PasswordResetToken token2 = passwordResetTokenRepository.findOneByUser(user2);
    assertNotNull(token2);
    assertNotEquals(token1.getId(), token2.getId());
  }

  @Test
  public void testForgotPasswordRollback() {
    willThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
        .given(notificationService)
        .send(any(NotificationRequest.class));

    forgotPassword().statusCode(500);

    User user = userRepository.findOne(UUID.fromString(DummyUserDto.AUTH_ID));
    assertNotNull(user);

    PasswordResetToken token = passwordResetTokenRepository.findOneByUser(user);
    assertNull(token);
  }

  @Test
  public void testCreatePasswordResetToken() {
    doNothing().when(permissionService).canManageUsers();

    UUID tokenId = passwordResetToken()
        .statusCode(200)
        .extract().as(UUID.class);

    PasswordResetToken token = passwordResetTokenRepository.findOne(tokenId);
    assertNotNull(token);
  }

  @Test
  public void shouldNotCreatePasswordResetTokenWhenUserHasNoPermission() {
    PermissionMessageException ex = mockUserManagePermissionError();

    passwordResetToken()
        .statusCode(403)
        .body(Fields.MESSAGE, equalTo(getMessage(ex.asMessage())));
  }

  private void testChangePassword(String password, String expectedMessage, int expectedCode) {
    String response = passwordReset(password, USER_TOKEN)
        .statusCode(expectedCode)
        .extract()
        .asString();

    assertThat(response, containsString(expectedMessage));
  }

  private ValidatableResponse passwordReset(String password, String token) {
    PasswordResetRequest passwordResetRequest = new PasswordResetRequest(
        DummyUserDto.USERNAME, password
    );

    return sendPostRequest(
        token, RESET_PASS_URL, passwordResetRequest, null
    );
  }

  private ValidatableResponse forgotPassword() {
    return sendPostRequest(
        null, FORGOT_PASS_URL, null, of(Fields.EMAIL, DummyUserDto.EMAIL)
    );
  }

  private ValidatableResponse changePassword(PasswordChangeRequest request) {
    return sendPostRequest(null, CHANGE_PASS_URL, request, null);
  }

  private ValidatableResponse passwordResetToken() {
    return sendPostRequest(
        USER_TOKEN, RESET_TOKEN_PASS_URL, null,
        of(Fields.USER_ID, DummyUserDto.REFERENCE_ID)
    );
  }


  private String login(String username, String password) {
    String token = startRequest()
        .auth()
        .preemptive()
        .basic("user-client", "changeme")
        .queryParam(Fields.PASSWORD, GrantTypes.PASSWORD)
        .queryParam(Fields.USERNAME, username)
        .queryParam(Fields.GRANT_TYPE, password)
        .when()
        .post(TOKEN_URL)
        .then()
        .statusCode(200)
        .extract()
        .path(Fields.ACCESS_TOKEN);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    return token;
  }

  private String logoutUser(Integer statusCode, String token) {
    return sendPostRequest(token, LOGOUT_URL, null, null)
        .statusCode(statusCode)
        .extract()
        .asString();
  }

  private PermissionMessageException mockUserManagePermissionError() {
    Message exMessage = new Message(ERROR_NO_FOLLOWING_PERMISSION, "USERS_MANAGE");

    PermissionMessageException ex = new PermissionMessageException(exMessage);
    doThrow(ex).when(permissionService).canManageUsers();

    return ex;
  }
}
