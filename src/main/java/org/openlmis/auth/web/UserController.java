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

import static org.openlmis.auth.i18n.MessageKeys.ERROR_TOKEN_EXPIRED;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_TOKEN_INVALID;
import static org.openlmis.auth.i18n.MessageKeys.USERS_LOGOUT_CONFIRMATION;
import static org.openlmis.auth.i18n.MessageKeys.USER_NOT_FOUND;
import static org.openlmis.auth.i18n.MessageKeys.USER_NOT_FOUND_BY_EMAIL;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.openlmis.auth.domain.PasswordResetToken;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.dto.PasswordResetRequestDto;
import org.openlmis.auth.dto.UserDto;
import org.openlmis.auth.exception.ValidationMessageException;
import org.openlmis.auth.i18n.ExposedMessageSource;
import org.openlmis.auth.repository.PasswordResetTokenRepository;
import org.openlmis.auth.repository.UserRepository;
import org.openlmis.auth.service.PasswordResetNotifier;
import org.openlmis.auth.service.PasswordResetRegistryService;
import org.openlmis.auth.service.PermissionService;
import org.openlmis.auth.service.UserService;
import org.openlmis.auth.service.notification.UserContactDetailsDto;
import org.openlmis.auth.service.notification.UserContactDetailsNotificationService;
import org.openlmis.auth.util.PasswordChangeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@Transactional
@RequestMapping("/api")
public class UserController {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private Validator validator;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @Autowired
  private ExposedMessageSource messageSource;

  @Autowired
  private TokenStore tokenStore;

  @Autowired
  private UserDtoValidator userDtoValidator;
  
  @Autowired
  private PasswordResetRequestDtoValidator passwordResetRequestDtoValidator;

  @Autowired
  private PasswordResetNotifier passwordResetNotifier;

  @Autowired
  private UserContactDetailsNotificationService userContactDetailsNotificationService;

  @Autowired
  private PasswordResetRegistryService passwordResetRegistryService;

  @InitBinder
  protected void initBinder(WebDataBinder binder) {
    binder.setValidator(this.validator);
  }

  /**
   * Custom endpoint for creating and updating users. Encrypts password with BCryptPasswordEncoder.
   *
   * @return saved user.
   */
  @RequestMapping(value = "/users/auth", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserDto saveUser(@RequestBody UserDto request, BindingResult bindingResult) {
    permissionService.canManageUsers(request.getId());
    LOGGER.debug("Creating or updating user");

    userDtoValidator.validate(request, bindingResult);

    if (bindingResult.hasErrors()) {
      throw new ValidationMessageException(bindingResult.getFieldError().getDefaultMessage());
    }

    return userService.saveUser(request);
  }

  /**
   * Gets a user by the given ID value. For security reasons the response does not contain password.
   */
  @RequestMapping(value = "/users/auth/{id}", method = RequestMethod.GET)
  @ResponseStatus(value = HttpStatus.OK)
  @ResponseBody
  public UserDto getUser(@PathVariable("id") UUID id) {
    permissionService.canManageUsers(id);
    User user = userRepository.findById(id).orElseThrow(
        () -> new ValidationMessageException(USER_NOT_FOUND)
    );

    UserDto dto = new UserDto();
    user.export(dto);

    dto.setPassword(null);

    return dto;
  }

  /**
   * Endpoint for logout.
   */
  @PreAuthorize("isAuthenticated()")
  @RequestMapping(value = "/users/auth/logout", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public String revokeToken(OAuth2Authentication auth) {
    OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();
    String token = details.getTokenValue();
    tokenStore.removeAccessToken(new DefaultOAuth2AccessToken(token));

    String[] msgArgs = {};
    return messageSource
        .getMessage(USERS_LOGOUT_CONFIRMATION, msgArgs, LocaleContextHolder.getLocale());
  }

  /**
   * Resets a user's password. If request fails returns map with field errors.
   */
  @RequestMapping(value = "/users/auth/passwordReset", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  public void passwordReset(@RequestBody PasswordResetRequestDto passwordResetRequestDto,
      BindingResult bindingResult) {
    permissionService.canEditUserPassword(passwordResetRequestDto.getUsername());
    
    passwordResetRequestDtoValidator.validate(passwordResetRequestDto, bindingResult);
    
    if (bindingResult.hasErrors()) {
      throw new ValidationMessageException(bindingResult.getFieldError().getCode());
    }

    String username = passwordResetRequestDto.getUsername();
    User user = userRepository.findOneByUsernameIgnoreCase(username);

    if (null == user) {
      throw new ValidationMessageException(USER_NOT_FOUND);
    }

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    user.setPassword(encoder.encode(passwordResetRequestDto.getNewPassword()));
    userRepository.save(user);
    LOGGER.debug("Password updated for user %s", username);
  }

  /**
   * Generates token which can be used to change user's password.
   */
  @RequestMapping(value = "/users/auth/forgotPassword", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  public void forgotPassword(@RequestParam(value = "email") String email) {
    List<UserContactDetailsDto> found = userContactDetailsNotificationService.findByEmail(email);

    if (CollectionUtils.isEmpty(found)) {
      LOGGER.error("User with provided email does not exist.",
          new ValidationMessageException(USER_NOT_FOUND_BY_EMAIL));
      return;
    }

    Optional<User> optionalUser = userRepository.findById(found.get(0).getReferenceDataUserId());
    if (!optionalUser.isPresent()) {
      LOGGER.error("User with ID {} does not exist.", found.get(0).getReferenceDataUserId(),
          new ValidationMessageException(USER_NOT_FOUND));
    } else {
      passwordResetRegistryService.checkPasswordResetLimit(optionalUser.get());
      passwordResetNotifier.sendNotification(optionalUser.get());
    }
  }

  /**
   * Changes user's password if valid reset token is provided.
   */
  @RequestMapping(value = "/users/auth/changePassword", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  public void showChangePasswordPage(
      @RequestBody PasswordChangeRequest passwordChangeRequest) {

    PasswordResetToken token =
        passwordResetTokenRepository.findById(passwordChangeRequest.getToken()).orElseThrow(
            () -> new ValidationMessageException(ERROR_TOKEN_INVALID)
        );

    if (token.isExpired()) {
      throw new ValidationMessageException(ERROR_TOKEN_EXPIRED);
    }

    User user = token.getUser();
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    user.setPassword(encoder.encode(passwordChangeRequest.getNewPassword()));
    userRepository.save(user);

    passwordResetTokenRepository.delete(token);
  }

  /**
   * Creates token which can be used to change user's password.
   */
  @RequestMapping(value = "/users/auth/passwordResetToken", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UUID generatePasswordResetToken(
      @RequestParam(value = "userId") UUID referenceDataUserId) {
    permissionService.canManageUsers(null);
    User user = userRepository.findById(referenceDataUserId).orElseThrow(
        () -> new ValidationMessageException(USER_NOT_FOUND)
    );

    PasswordResetToken token = passwordResetNotifier.createPasswordResetToken(user);

    return token.getId();
  }
}
