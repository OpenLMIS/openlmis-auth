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
import static org.hamcrest.Matchers.is;
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
import static org.mockito.Mockito.doThrow;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;
import static org.openlmis.auth.i18n.MessageKeys.USERS_PASSWORD_RESET_INVALID_VALUE;
import static org.openlmis.auth.i18n.MessageKeys.USER_NOT_FOUND;
import static org.openlmis.auth.service.ExpirationTokenNotifier.TOKEN_VALIDITY_HOURS;
import static org.openlmis.auth.web.TestWebData.Tokens.USER_TOKEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import com.jayway.restassured.response.ValidatableResponse;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.auth.DummyUserMainDetailsDto;
import org.openlmis.auth.domain.PasswordResetToken;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.dto.LocalizedMessageDto;
import org.openlmis.auth.dto.UserDto;
import org.openlmis.auth.dto.referencedata.UserMainDetailsDto;
import org.openlmis.auth.exception.ExternalApiException;
import org.openlmis.auth.exception.PermissionMessageException;
import org.openlmis.auth.repository.PasswordResetTokenRepository;
import org.openlmis.auth.repository.UserRepository;
import org.openlmis.auth.service.PermissionService;
import org.openlmis.auth.service.notification.NotificationService;
import org.openlmis.auth.util.Message;
import org.openlmis.auth.util.PasswordChangeRequest;
import org.openlmis.auth.web.TestWebData.Fields;
import org.openlmis.auth.web.TestWebData.GrantTypes;
import org.openlmis.util.PasswordResetRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.HttpServerErrorException;

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

  @MockBean
  private PermissionService permissionService;

  @MockBean
  private NotificationService notificationService;

  private User user;
  private UserMainDetailsDto admin = new DummyUserMainDetailsDto();

  @Before
  public void setUp() {
    given(userReferenceDataService.findUserByEmail(admin.getEmail()))
        .willReturn(admin);
    given(userReferenceDataService.findOne(admin.getId()))
        .willReturn(admin);
    given(userReferenceDataService.putUser(any(UserMainDetailsDto.class)))
        .willReturn(admin);

    willDoNothing().given(notificationService).notify(
        any(User.class),
        any(String.class),
        any(String.class)
    );

    passwordResetTokenRepository.deleteAll();
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    user = userRepository.findOne(admin.getId());
    user.setPassword(encoder.encode(DummyUserMainDetailsDto.PASSWORD));
    userRepository.save(user);
  }

  @Test
  public void shouldSaveUser() {
    sendPostRequest(USER_TOKEN, RESOURCE_URL, new UserDto(user, admin), null)
        .contentType(is(APPLICATION_JSON_UTF8_VALUE))
        .statusCode(200);
  }

  @Test
  public void shouldNotSaveUserWhenUserHasNoPermission() {
    PermissionMessageException ex = mockUserManagePermissionError();

    sendPostRequest(USER_TOKEN, RESOURCE_URL, new UserDto(user, admin), null)
        .contentType(is(APPLICATION_JSON_UTF8_VALUE))
        .statusCode(403)
        .body(Fields.MESSAGE, equalTo(getMessage(ex.asMessage())));
  }

  @Test
  public void shouldPassErrorMessageFromExternalServiceIfThereWereProblemsWithUserSave() {
    LocalizedMessageDto localizedMessage = new LocalizedMessageDto("test.key", "test.message");
    doThrow(new ExternalApiException(null, localizedMessage))
        .when(userReferenceDataService)
        .putUser(any(UserMainDetailsDto.class));

    sendPostRequest(USER_TOKEN, RESOURCE_URL, new UserDto(user, admin), null)
        .contentType(is(APPLICATION_JSON_UTF8_VALUE))
        .statusCode(400)
        .body(Fields.MESSAGE_KEY, is("test.key"))
        .body(Fields.MESSAGE, is("test.message"));
  }

  @Test
  public void shouldResetPassword() {
    String password = userRepository
        .findOne(admin.getId())
        .getPassword();
    assertNotNull(password);

    passwordReset("test1234", USER_TOKEN).statusCode(200);

    String newPassword = userRepository
        .findOne(admin.getId())
        .getPassword();
    assertNotNull(newPassword);
    assertNotEquals(password, newPassword);
  }

  @Test
  public void shouldReturnErrorMessageIfPasswordIsTooShort() {
    checkErrorResponseForPasswordReset("1234567", "size must be between 8 and 16");
  }

  @Test
  public void shouldReturnErrorMessageIfPasswordIsTooLong() {
    checkErrorResponseForPasswordReset("sdokfsodpfjsaidjasj2akdsjk",
        "size must be between 8 and 16");
  }

  @Test
  public void shouldReturnErrorMessageIfPasswordDoesNotConstainNumber() {
    checkErrorResponseForPasswordReset("vvvvvvvvvvv", "must contain at least 1 number");
  }

  @Test
  public void shouldReturnErrorMessageIfPasswordContainsSpaces() {
    checkErrorResponseForPasswordReset("1sample text", "must not contain spaces");
  }

  @Test
  public void shouldNotResetPasswordForOtherUserWithoutPermissions() {
    PermissionMessageException ex = buildUserManagerPermissionError();
    String differentUsername = "differentUser";

    doThrow(ex).when(permissionService).canEditUserPassword(differentUsername);

    passwordReset(differentUsername, "newpassword123", USER_TOKEN)
        .statusCode(403)
        .body(Fields.MESSAGE, equalTo(getMessage(ex.asMessage())));
  }

  @Test
  public void shouldReturnBadRequestWhenUserNotFound() {
    PasswordResetRequest passwordResetRequest = new PasswordResetRequest(
        "wrongUser", "newpassword1"
    );

    sendPostRequest(USER_TOKEN, RESET_PASS_URL, passwordResetRequest, null)
        .contentType(is(APPLICATION_JSON_UTF8_VALUE))
        .statusCode(400)
        .body(Fields.MESSAGE_KEY, equalTo(USER_NOT_FOUND));
  }

  @Test
  public void revokeTokenTest() {
    String accessToken = login(DummyUserMainDetailsDto.USERNAME, DummyUserMainDetailsDto.PASSWORD);
    String response = logoutUser(200, accessToken);

    assertTrue(response.contains("You have successfully logged out!"));

    logoutUser(401, accessToken);
  }

  @Test
  public void testForgotPassword() {
    forgotPassword().statusCode(200);

    User found = userRepository.findOne(admin.getId());
    assertNotNull(found);

    PasswordResetToken token = passwordResetTokenRepository.findOneByUser(found);
    assertNotNull(token);

    PasswordChangeRequest request = new PasswordChangeRequest(token.getId(), "test");
    changePassword(request).statusCode(200);

    User changedUser = userRepository.findOne(admin.getId());
    assertNotNull(changedUser);
    assertNotEquals(changedUser.getPassword(), found.getPassword());
  }

  @Test
  public void shouldCreateNewTokenAfterEachForgotPasswordRequest() {
    User user1 = userRepository.findOne(admin.getId());
    assertNotNull(user1);

    PasswordResetToken token1 = new PasswordResetToken();
    token1.setUser(user1);
    token1.setExpiryDate(ZonedDateTime.now().plusHours(TOKEN_VALIDITY_HOURS));

    passwordResetTokenRepository.save(token1);

    forgotPassword().statusCode(200);

    User user2 = userRepository.findOne(admin.getId());
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
        .notify(any(User.class), any(String.class), any(String.class));

    forgotPassword().statusCode(500);

    User found = userRepository.findOne(admin.getId());
    assertNotNull(found);

    PasswordResetToken token = passwordResetTokenRepository.findOneByUser(found);
    assertNull(token);
  }

  @Test
  public void testCreatePasswordResetToken() {
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

  private void checkErrorResponseForPasswordReset(String password, String expectedMessage) {
    passwordReset(password, USER_TOKEN)
        .statusCode(400)
        .body("messageKey", equalTo(USERS_PASSWORD_RESET_INVALID_VALUE))
        .body("message", containsString(expectedMessage));
  }

  private ValidatableResponse passwordReset(String password, String token) {
    return passwordReset(DummyUserMainDetailsDto.USERNAME, password, token);
  }

  private ValidatableResponse passwordReset(String username, String password, String token) {
    PasswordResetRequest passwordResetRequest = new PasswordResetRequest(
        username, password
    );

    return sendPostRequest(token, RESET_PASS_URL, passwordResetRequest, null)
        .contentType(is(APPLICATION_JSON_UTF8_VALUE));
  }

  private ValidatableResponse forgotPassword() {
    return sendPostRequest(null, FORGOT_PASS_URL, null,
        of(Fields.EMAIL, DummyUserMainDetailsDto.EMAIL))
        .contentType(is(APPLICATION_JSON_UTF8_VALUE));
  }

  private ValidatableResponse changePassword(PasswordChangeRequest request) {
    return sendPostRequest(null, CHANGE_PASS_URL, request, null)
        .contentType(is(APPLICATION_JSON_UTF8_VALUE));
  }

  private ValidatableResponse passwordResetToken() {
    return sendPostRequest(
        USER_TOKEN, RESET_TOKEN_PASS_URL, null,
        of(Fields.USER_ID, DummyUserMainDetailsDto.REFERENCE_ID))
        .contentType(is(APPLICATION_JSON_UTF8_VALUE));
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
    PermissionMessageException ex = buildUserManagerPermissionError();

    doThrow(ex).when(permissionService).canManageUsers(any(UUID.class));

    return ex;
  }

  private PermissionMessageException buildUserManagerPermissionError() {
    Message exMessage = new Message(ERROR_NO_FOLLOWING_PERMISSION, "USERS_MANAGE");

    return new PermissionMessageException(exMessage);
  }
}
