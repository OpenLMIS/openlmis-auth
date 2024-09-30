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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.auth.dto.PasswordResetRequestDto;
import org.openlmis.auth.dto.referencedata.UserMainDetailsDto;
import org.openlmis.auth.i18n.ExposedMessageSource;
import org.openlmis.auth.i18n.MessageKeys;
import org.openlmis.auth.service.referencedata.UserReferenceDataService;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@RunWith(MockitoJUnitRunner.class)
public class PasswordResetRequestDtoValidatorTest {

  private static final String USERNAME = "username";
  private static final String PASS_FIELD = "newPassword";

  @Mock
  private ExposedMessageSource messageSource;

  @Mock
  private UserReferenceDataService userReferenceDataService;

  @Mock
  private PasswordStrengthValidator passwordStrengthValidator;

  @InjectMocks
  private Validator validator = new PasswordResetRequestDtoValidator();

  private PasswordResetRequestDto request;
  private Errors errors;
  private UserMainDetailsDto userMainDetails;

  @Before
  public void setUp() throws Exception {
    request = new PasswordResetRequestDto();
    request.setNewPassword("testpassword1");
    request.setUsername("testusername");

    userMainDetails = new UserMainDetailsDto();
    userMainDetails.setUsername(request.getUsername());
    userMainDetails.setFirstName("testfirstname");
    userMainDetails.setLastName("testlastname");

    errors = new BeanPropertyBindingResult(request, "request");

    when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, String.class));
    when(userReferenceDataService.findUser(request.getUsername()))
        .thenReturn(userMainDetails);
    doNothing().when(passwordStrengthValidator).verifyPasswordStrength(anyString());
  }

  @Test
  public void shouldNotFindErrorsWhenUserIsValid() {
    validator.validate(request, errors);

    assertThat(errors.getErrorCount()).isEqualTo(0);
  }

  @Test
  public void shouldRejectWhenUsernameIsNull() {
    request.setUsername(null);

    validator.validate(request, errors);

    assertErrorMessage(errors, USERNAME, MessageKeys.ERROR_FIELD_REQUIRED);
  }

  @Test
  public void shouldRejectWhenPasswordIsNull() {
    request.setNewPassword(null);

    validator.validate(request, errors);

    assertErrorMessage(errors, PASS_FIELD, MessageKeys.ERROR_FIELD_REQUIRED);
  }

  @Test
  public void shouldRejectWhenPasswordContainsSpace() {
    request.setNewPassword("testpa sswrd");

    validator.validate(request, errors);

    assertErrorMessage(errors, PASS_FIELD, MessageKeys.USERS_PASSWORD_RESET_CONTAIN_SPACES);
  }

  @Test
  public void shouldRejectWhenPasswordNotContainNumber() {
    request.setNewPassword("testpasswrd");

    validator.validate(request, errors);

    assertErrorMessage(errors, PASS_FIELD,
        MessageKeys.USERS_PASSWORD_RESET_NOT_CONTAIN_NUMBER);
  }

  @Test
  public void shouldRejectWhenPasswordIsNotProperSize() {
    request.setNewPassword("asd");

    validator.validate(request, errors);

    assertErrorMessage(errors, PASS_FIELD,
        MessageKeys.USERS_PASSWORD_RESET_INVALID_PASSWORD_LENGTH);
  }

  @Test
  public void shouldRejectWhenPasswordContainsUsername() {
    request.setNewPassword("testusername1");

    validator.validate(request, errors);

    assertErrorMessage(errors, PASS_FIELD, MessageKeys.USERS_PASSWORD_CONTAIN_USER_DETAILS);
  }

  @Test
  public void shouldRejectWhenPasswordContainsFirstName() {
    request.setNewPassword("testfirstname1");

    validator.validate(request, errors);

    assertErrorMessage(errors, PASS_FIELD, MessageKeys.USERS_PASSWORD_CONTAIN_USER_DETAILS);
  }

  @Test
  public void shouldRejectWhenPasswordContainsLastName() {
    request.setNewPassword("testlastname1");

    validator.validate(request, errors);

    assertErrorMessage(errors, PASS_FIELD, MessageKeys.USERS_PASSWORD_CONTAIN_USER_DETAILS);
  }

  private void assertErrorMessage(Errors errors, String field, String expectedMessage) {
    assertThat(errors.hasFieldErrors(field)).as(
        "There is no errors for field: " + field).isTrue();

    boolean match = errors.getFieldErrors(field)
        .stream()
        .anyMatch(e -> e.getField().equals(field) && e.getDefaultMessage().equals(expectedMessage));

    assertThat(match).as("There is no error with default message: "
        + expectedMessage).isTrue();
  }
}
