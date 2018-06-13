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
import static org.openlmis.auth.service.PermissionService.USERS_MANAGE;
import static org.openlmis.auth.web.UserSaveRequestValidator.ACTIVE;
import static org.openlmis.auth.web.UserSaveRequestValidator.ALLOW_NOTIFY;
import static org.openlmis.auth.web.UserSaveRequestValidator.ENABLED;
import static org.openlmis.auth.web.UserSaveRequestValidator.EXTRA_DATA;
import static org.openlmis.auth.web.UserSaveRequestValidator.HOME_FACILITY_ID;
import static org.openlmis.auth.web.UserSaveRequestValidator.JOB_TITLE;
import static org.openlmis.auth.web.UserSaveRequestValidator.LOGIN_RESTRICTED;
import static org.openlmis.auth.web.UserSaveRequestValidator.ROLE_ASSIGNMENTS;
import static org.openlmis.auth.web.UserSaveRequestValidator.TIMEZONE;
import static org.openlmis.auth.web.UserSaveRequestValidator.USERNAME;
import static org.openlmis.auth.web.UserSaveRequestValidator.VERIFIED;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.Locale;
import java.util.UUID;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.auth.DummyUserDto;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.dto.UserSaveRequest;
import org.openlmis.auth.dto.referencedata.RoleAssignmentDto;
import org.openlmis.auth.dto.referencedata.UserDto;
import org.openlmis.auth.i18n.ExposedMessageSource;
import org.openlmis.auth.i18n.MessageKeys;
import org.openlmis.auth.repository.UserRepository;
import org.openlmis.auth.service.PermissionService;
import org.openlmis.auth.service.referencedata.UserReferenceDataService;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class UserSaveRequestValidatorTest {

  @Mock
  private PermissionService permissionService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserReferenceDataService userReferenceDataService;

  @Mock
  private ExposedMessageSource messageSource;

  @InjectMocks
  private Validator validator = new UserSaveRequestValidator();

  private User user;
  private UserDto userDto;
  private UserSaveRequest request;
  private Errors errors;

  @Before
  public void setUp() {
    userDto = new DummyUserDto();

    user = new User();
    user.setUsername(userDto.getUsername());
    user.setEnabled(true);

    request = new UserSaveRequest(user, userDto);

    errors = new BeanPropertyBindingResult(request, "request");

    when(permissionService.hasRight(USERS_MANAGE)).thenReturn(true);
    when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class)))
        .thenAnswer(invocation -> invocation.getArgumentAt(0, String.class));
    when(userReferenceDataService.findOne(request.getId()))
        .thenReturn(userDto);
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
  public void shouldRejectWhenUsernameIsEmpty() {
    request.setUsername("");

    validator.validate(request, errors);

    assertErrorMessage(errors, USERNAME, MessageKeys.ERROR_FIELD_REQUIRED);
  }

  @Test
  public void shouldRejectWhenUsernameContainsWhitespace() {
    request.setUsername("user name");

    validator.validate(request, errors);

    assertErrorMessage(errors, USERNAME, MessageKeys.ERROR_USERNAME_INVALID);
  }

  @Test
  public void shouldRejectWhenVerifiedFlagWasChanged() {
    request.setVerified(!userDto.isVerified());

    validator.validate(request, errors);

    assertErrorMessage(errors, VERIFIED, MessageKeys.ERROR_FIELD_IS_INVARIANT);
  }

  @Test
  public void shouldNotRejectIfUserHasNoRightForEditAndFieldsWereNotChanged() {
    prepareForValidateInvariants();

    validator.validate(request, errors);

    assertThat(errors.getErrorCount()).isEqualTo(0);
  }

  @Test
  public void shouldNotRejectIfUserHasNoRightForEditAndBasicDetailsWereChanged() {
    prepareForValidateInvariants();

    request.setFirstName(RandomStringUtils.randomAlphanumeric(5));
    request.setLastName(RandomStringUtils.randomAlphanumeric(5));
    request.setEmail(RandomStringUtils.randomAlphanumeric(1) + request.getEmail());
    request.setPhoneNumber(RandomStringUtils.randomNumeric(9));
    validator.validate(request, errors);

    assertThat(errors.getErrorCount()).isEqualTo(0);
  }

  @Test
  public void shouldRejectIfUserHasNoRightForEditAndInvariantsWereChanged() {
    prepareForValidateInvariants();

    request.setUsername(RandomStringUtils.randomAlphanumeric(10));
    request.setJobTitle("test-job-title");
    request.setTimezone("test-time-zone");
    request.setHomeFacilityId(UUID.randomUUID());
    request.setActive(!userDto.isActive());
    request.setLoginRestricted(!userDto.isLoginRestricted());
    request.setAllowNotify(!userDto.getAllowNotify());
    request.setExtraData(ImmutableMap.of("a", "b"));
    request.setRoleAssignments(Sets.newHashSet(new RoleAssignmentDto()));
    request.setEnabled(!user.getEnabled());
    validator.validate(request, errors);

    assertThat(errors.getErrorCount()).isGreaterThanOrEqualTo(9);
    assertErrorMessage(errors, ENABLED, MessageKeys.ERROR_FIELD_IS_INVARIANT);
    assertErrorMessage(errors, USERNAME, MessageKeys.ERROR_FIELD_IS_INVARIANT);
    assertErrorMessage(errors, JOB_TITLE, MessageKeys.ERROR_FIELD_IS_INVARIANT);
    assertErrorMessage(errors, TIMEZONE, MessageKeys.ERROR_FIELD_IS_INVARIANT);
    assertErrorMessage(errors, HOME_FACILITY_ID, MessageKeys.ERROR_FIELD_IS_INVARIANT);
    assertErrorMessage(errors, ACTIVE, MessageKeys.ERROR_FIELD_IS_INVARIANT);
    assertErrorMessage(errors, LOGIN_RESTRICTED, MessageKeys.ERROR_FIELD_IS_INVARIANT);
    assertErrorMessage(errors, ALLOW_NOTIFY, MessageKeys.ERROR_FIELD_IS_INVARIANT);
    assertErrorMessage(errors, EXTRA_DATA, MessageKeys.ERROR_FIELD_IS_INVARIANT);
    assertErrorMessage(errors, ROLE_ASSIGNMENTS, MessageKeys.ERROR_FIELD_IS_INVARIANT);
  }

  private void prepareForValidateInvariants() {
    when(permissionService.hasRight(USERS_MANAGE)).thenReturn(false);
    when(userRepository.findOne(request.getId()))
        .thenReturn(user);
  }

  private void assertErrorMessage(Errors errors, String field, String expectedMessage) {
    assertThat(errors.hasFieldErrors(field)).as("There is no errors for field: " + field).isTrue();

    boolean match = errors.getFieldErrors(field)
        .stream()
        .anyMatch(e -> e.getField().equals(field) && e.getDefaultMessage().equals(expectedMessage));

    assertThat(match).as("There is no error with default message: " + expectedMessage).isTrue();
  }
}
