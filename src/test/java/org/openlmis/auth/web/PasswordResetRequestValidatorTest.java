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
import static org.mockito.Mockito.when;

import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.auth.i18n.ExposedMessageSource;
import org.openlmis.auth.i18n.MessageKeys;
import org.openlmis.auth.util.PasswordResetRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@RunWith(MockitoJUnitRunner.class)
public class PasswordResetRequestValidatorTest {

  private static final String USERNAME = "username";
  private static final String NEW_PASSWORD = "newPassword";

  @Mock
  private ExposedMessageSource messageSource;

  @InjectMocks
  private Validator validator = new PasswordResetRequestValidator();

  private PasswordResetRequest request;
  private Errors errors;

  @Before
  public void setUp() throws Exception {
    request = new PasswordResetRequest();
    request.setNewPassword("testpassword1");
    request.setUsername("testusername");

    errors = new BeanPropertyBindingResult(request, "request");

    when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class)))
        .thenAnswer(invocation -> invocation.getArgumentAt(0, String.class));
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

    assertErrorMessage(errors, NEW_PASSWORD, MessageKeys.ERROR_FIELD_REQUIRED);
  }

  @Test
  public void shouldRejectWhenPasswordContainsSpace() {
    request.setNewPassword("testpa sswrd");

    validator.validate(request, errors);

    assertErrorMessage(errors, NEW_PASSWORD, MessageKeys.USERS_PASSWORD_RESET_CONTAIN_SPACES);
  }

  @Test
  public void shouldRejectWhenPasswordNotContainNumber() {
    request.setNewPassword("testpasswrd");

    validator.validate(request, errors);

    assertErrorMessage(errors, NEW_PASSWORD,
        MessageKeys.USERS_PASSWORD_RESET_NOT_CONTAIN_NUMBER);
  }

  @Test
  public void shouldRejectWhenPasswordIsNotProperSize() {
    request.setNewPassword("asd");

    validator.validate(request, errors);

    assertErrorMessage(errors, NEW_PASSWORD,
        MessageKeys.USERS_PASSWORD_RESET_INVALID_PASSWORD_LENGTH);
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
