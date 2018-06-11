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

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.dto.UserSaveRequest;
import org.openlmis.auth.dto.referencedata.RoleAssignmentDto;
import org.openlmis.auth.dto.referencedata.UserDto;
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
 * A validator for {@link org.openlmis.auth.dto.UserSaveRequest} object.
 */
@Component
public class UserSaveRequestValidator implements BaseValidator {

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  // User fields
  static final String USERNAME = "username";
  static final String JOB_TITLE = "jobTitle";
  static final String TIMEZONE = "timezone";
  static final String HOME_FACILITY_ID = "homeFacilityId";
  static final String VERIFIED = "verified";
  static final String ACTIVE = "active";
  static final String LOGIN_RESTRICTED = "loginRestricted";
  static final String ALLOW_NOTIFY = "allowNotify";
  static final String EXTRA_DATA = "extraData";
  static final String ROLE_ASSIGNMENTS = "roleAssignments";
  static final String ENABLED = "enabled";

  /**
   * Checks if the given class definition is supported.
   *
   * @param clazz the {@link Class} that this {@link Validator} is being asked if it can {@link
   * #validate(Object, Errors) validate}
   * @return true if {@code clazz} is equal to {@link UserSaveRequest}. Otherwise false.
   */
  @Override
  public boolean supports(Class<?> clazz) {
    return UserSaveRequest.class.equals(clazz);
  }

  /**
   * Validates the {@code target} object, which must be an instance of {@link UserSaveRequest}
   * class.
   *
   * @param target the object that is to be validated (never {@code null})
   * @param errors contextual state about the validation process (never {@code null})
   * @see ValidationUtils
   */
  @Override
  public void validate(Object target, Errors errors) {
    rejectIfEmptyOrWhitespace(errors, USERNAME, MessageKeys.ERROR_FIELD_REQUIRED);

    if (!errors.hasErrors()) {
      UserSaveRequest dto = (UserSaveRequest) target;

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

  private void validateInvariants(UserSaveRequest dto, Errors errors) {
    User db = userRepository.findOneByReferenceDataUserId(dto.getId());

    rejectIfInvariantWasChanged(errors, ENABLED, db.getEnabled(), dto.getEnabled());

    UserDto reference = userReferenceDataService.findOne(dto.getId());

    rejectIfInvariantWasChanged(errors, USERNAME, reference.getUsername(), dto.getUsername());
    rejectIfInvariantWasChanged(errors, JOB_TITLE, reference.getJobTitle(), dto.getJobTitle());
    rejectIfInvariantWasChanged(errors, TIMEZONE, reference.getTimezone(), dto.getTimezone());
    rejectIfInvariantWasChanged(errors, HOME_FACILITY_ID,
        reference.getHomeFacilityId(), dto.getHomeFacilityId());
    rejectIfInvariantWasChanged(errors, VERIFIED, reference.isVerified(), dto.isVerified());
    rejectIfInvariantWasChanged(errors, ACTIVE, reference.isActive(), dto.isActive());
    rejectIfInvariantWasChanged(errors, LOGIN_RESTRICTED,
        reference.isLoginRestricted(), dto.isLoginRestricted());
    rejectIfInvariantWasChanged(errors, ALLOW_NOTIFY,
        reference.getAllowNotify(), dto.getAllowNotify());
    rejectIfInvariantWasChanged(errors, EXTRA_DATA, reference.getExtraData(), dto.getExtraData());

    Set<RoleAssignmentDto> oldRoleAssignments = Optional
        .ofNullable(reference.getRoleAssignments())
        .orElse(Collections.emptySet());
    Set<RoleAssignmentDto> newRoleAssignments = Optional
        .ofNullable(dto.getRoleAssignments())
        .orElse(Collections.emptySet());

    rejectIfInvariantWasChanged(errors, ROLE_ASSIGNMENTS, oldRoleAssignments, newRoleAssignments);
  }

  private void rejectIfInvariantWasChanged(Errors errors, String field, Object oldValue,
      Object newValue) {
    rejectIfNotEqual(errors, oldValue, newValue, field, MessageKeys.ERROR_FIELD_IS_INVARIANT);
  }
}
