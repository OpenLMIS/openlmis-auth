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

import static org.openlmis.auth.service.PermissionService.USERS_MANAGE;

import org.openlmis.auth.domain.User;
import org.openlmis.auth.dto.UserDto;
import org.openlmis.auth.dto.referencedata.UserMainDetailsDto;
import org.openlmis.auth.i18n.MessageKeys;
import org.openlmis.auth.repository.UserRepository;
import org.openlmis.auth.service.PermissionService;
import org.openlmis.auth.service.referencedata.UserReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * A validator for {@link UserDto} object.
 */
@Component
public class UserDtoValidator extends BaseValidator {

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  // User fields
  static final String ID = "id";
  static final String USERNAME = "username";
  static final String ENABLED = "enabled";

  /**
   * Checks if the given class definition is supported.
   *
   * @param clazz the {@link Class} that this {@link Validator} is being asked if it can {@link
   * #validate(Object, Errors) validate}
   * @return true if {@code clazz} is equal to {@link UserDto}. Otherwise false.
   */
  @Override
  public boolean supports(Class<?> clazz) {
    return UserDto.class.equals(clazz);
  }

  /**
   * Validates the {@code target} object, which must be an instance of
   * {@link UserDto} class.
   *
   * @param target the object that is to be validated (never {@code null})
   * @param errors contextual state about the validation process (never {@code null})
   * @see ValidationUtils
   */
  @Override
  public void validate(Object target, Errors errors) {
    rejectIfEmptyOrWhitespace(errors, ID, MessageKeys.ERROR_FIELD_REQUIRED);
    rejectIfEmptyOrWhitespace(errors, USERNAME, MessageKeys.ERROR_FIELD_REQUIRED);

    if (!errors.hasErrors()) {
      UserDto dto = (UserDto) target;

      verifyReferenceDataUserId(dto, errors);

      if (null != dto.getId() && !permissionService.hasRight(USERS_MANAGE)) {
        validateInvariants(dto, errors);
      }

      verifyUsername(dto.getUsername(), errors);
    }
  }

  private void verifyUsername(String username, Errors errors) {
    // user name cannot contains invalid characters
    if (!username.matches("\\w+")) {
      rejectValue(errors, USERNAME, MessageKeys.ERROR_USERNAME_INVALID);
    }
  }

  private void verifyReferenceDataUserId(UserDto dto, Errors errors) {
    UserMainDetailsDto referenceDataUser = userReferenceDataService
        .findOne(dto.getId());

    if (null == referenceDataUser) {
      rejectValue(errors, ID, MessageKeys.ERROR_USER_NOT_FOUND);
    }
  }

  private void validateInvariants(UserDto dto, Errors errors) {
    User db = userRepository.findOne(dto.getId());

    rejectIfInvariantWasChanged(errors, ENABLED, db.getEnabled(), dto.getEnabled());
    rejectIfInvariantWasChanged(errors, USERNAME, db.getUsername(), dto.getUsername());
  }

  private void rejectIfInvariantWasChanged(Errors errors, String field, Object oldValue,
      Object newValue) {
    rejectIfNotEqual(errors, oldValue, newValue, field, MessageKeys.ERROR_FIELD_IS_INVARIANT);
  }

}
