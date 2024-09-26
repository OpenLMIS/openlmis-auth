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

import org.openlmis.auth.dto.PasswordResetRequestDto;
import org.openlmis.auth.dto.referencedata.UserMainDetailsDto;
import org.openlmis.auth.i18n.MessageKeys;
import org.openlmis.auth.service.referencedata.UserReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class PasswordResetRequestDtoValidator extends BaseValidator {

  private static final String USERNAME = "username";
  private static final String PASS_FIELD = "newPassword";
  private static final String REGEX_CONTAINS_NUMBER = "(?=.*[0-9]).+";
  private static final String REGEX_CONTAINS_SPACES = "(?=\\S+$).+";
  private static final String REGEX_SIZE_IS_BETWEEN_8_AND_72 = "^[a-zA-Z0-9]{8,72}$";

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private PasswordStrengthValidator passwordStrengthValidator;

  @Override
  public boolean supports(Class<?> clazz) {
    return PasswordResetRequestDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    rejectIfEmptyOrWhitespace(errors, USERNAME, MessageKeys.ERROR_FIELD_REQUIRED);
    rejectIfEmptyOrWhitespace(errors, PASS_FIELD, MessageKeys.ERROR_FIELD_REQUIRED);

    if (!errors.hasErrors()) {
      PasswordResetRequestDto passwordResetRequestDto = (PasswordResetRequestDto) target;
      verifyPassword(passwordResetRequestDto, errors);
      passwordStrengthValidator.verifyPasswordStrength(passwordResetRequestDto.getNewPassword());
    }
  }

  private void verifyPassword(PasswordResetRequestDto passwordResetRequestDto, Errors errors) {
    String password = passwordResetRequestDto.getNewPassword();
    if (!password.matches(REGEX_CONTAINS_NUMBER)) {
      rejectValue(errors, PASS_FIELD, MessageKeys.USERS_PASSWORD_RESET_NOT_CONTAIN_NUMBER);
    }
    if (!password.matches(REGEX_CONTAINS_SPACES)) {
      rejectValue(errors, PASS_FIELD, MessageKeys.USERS_PASSWORD_RESET_CONTAIN_SPACES);
    }
    if (!password.matches(REGEX_SIZE_IS_BETWEEN_8_AND_72)) {
      rejectValue(errors, PASS_FIELD, MessageKeys.USERS_PASSWORD_RESET_INVALID_PASSWORD_LENGTH);
    }
    UserMainDetailsDto userDetails =
        userReferenceDataService.findUser(passwordResetRequestDto.getUsername());
    if (containsUserDetails(password, userDetails.getUsername(),
        userDetails.getFirstName(), userDetails.getLastName())) {
      rejectValue(errors, PASS_FIELD, MessageKeys.USERS_PASSWORD_CONTAIN_USER_DETAILS);
    }
  }

  private boolean containsUserDetails(String password, String username, String firstName,
      String secondName) {
    return password.toLowerCase().contains(username.toLowerCase())
        || password.toLowerCase().contains(firstName.toLowerCase())
        || password.toLowerCase().contains(secondName.toLowerCase());
  }

}
