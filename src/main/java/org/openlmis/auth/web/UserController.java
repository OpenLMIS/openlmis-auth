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
import static org.openlmis.auth.i18n.MessageKeys.USERS_FORGOT_PASSWORD_USER_NOT_FOUND;
import static org.openlmis.auth.i18n.MessageKeys.USERS_LOGOUT_CONFIRMATION;
import static org.openlmis.auth.i18n.MessageKeys.USERS_PASSWORD_RESET_CONFIRMATION;
import static org.openlmis.auth.i18n.MessageKeys.USERS_PASSWORD_RESET_USER_NOT_FOUND;

import org.openlmis.auth.domain.PasswordResetToken;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.dto.referencedata.UserDto;
import org.openlmis.auth.exception.BindingResultException;
import org.openlmis.auth.exception.ValidationMessageException;
import org.openlmis.auth.i18n.ExposedMessageSource;
import org.openlmis.auth.repository.PasswordResetTokenRepository;
import org.openlmis.auth.repository.UserRepository;
import org.openlmis.auth.service.PermissionService;
import org.openlmis.auth.service.UserService;
import org.openlmis.auth.service.referencedata.UserReferenceDataService;
import org.openlmis.auth.util.PasswordChangeRequest;
import org.openlmis.util.PasswordResetRequest;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

@Controller
@Transactional
@RequestMapping("/api")
public class UserController {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private Validator validator;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private ExposedMessageSource messageSource;

  @Autowired
  private TokenStore tokenStore;

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
  public User saveUser(@RequestBody User user, BindingResult bindingResult) {
    permissionService.canManageUsers();
    LOGGER.debug("Creating or updating user");

    if (bindingResult.getErrorCount() == 0) {
      return userService.saveUser(user);
    } else {
      throw new BindingResultException(getErrors(bindingResult));
    }
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
   * Resets a user's password.
   *
   * @return confirmation message or map with field errors.
   */
  @RequestMapping(value = "/users/auth/passwordReset", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public String passwordReset(
      @RequestBody @Valid PasswordResetRequest passwordResetRequest, BindingResult bindingResult) {
    permissionService.canManageUsers();
    Map<String, String> errors = new HashMap<>();

    if (!bindingResult.hasErrors()) {
      String username = passwordResetRequest.getUsername();
      Optional<User> user = userRepository.findOneByUsername(username);

      if (user.isPresent()) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.get().setPassword(encoder.encode(passwordResetRequest.getNewPassword()));
        userRepository.save(user.get());

        String[] msgArgs = {username};
        return messageSource.getMessage(
            USERS_PASSWORD_RESET_CONFIRMATION, msgArgs, LocaleContextHolder.getLocale());
      } else {
        String[] msgArgs = {};
        errors.put("username", messageSource.getMessage(USERS_PASSWORD_RESET_USER_NOT_FOUND,
            msgArgs, LocaleContextHolder.getLocale()));
      }
    } else {
      errors.putAll(getErrors(bindingResult));
    }
    throw new BindingResultException(errors);
  }

  /**
   * Generates token which can be used to change user's password.
   */
  @RequestMapping(value = "/users/auth/forgotPassword", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public void forgotPassword(@RequestParam(value = "email") String email) {
    UserDto refDataUser = userReferenceDataService.findUserByEmail(email);

    if (refDataUser == null) {
      throw new ValidationMessageException(USERS_FORGOT_PASSWORD_USER_NOT_FOUND);
    }

    User user = userRepository.findOneByReferenceDataUserId(refDataUser.getId());
    userService.sendResetPasswordEmail(user, email, false);
  }

  /**
   * Changes user's password if valid reset token is provided.
   */
  @RequestMapping(value = "/users/auth/changePassword", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  public void showChangePasswordPage(
      @RequestBody PasswordChangeRequest passwordChangeRequest) {

    PasswordResetToken token =
        passwordResetTokenRepository.findOne(passwordChangeRequest.getToken());

    if (token == null) {
      throw new ValidationMessageException(ERROR_TOKEN_INVALID);
    }

    if (token.getExpiryDate().isBefore(ZonedDateTime.now())) {
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
    permissionService.canManageUsers();
    User user = userRepository.findOneByReferenceDataUserId(referenceDataUserId);

    PasswordResetToken token = userService.createPasswordResetToken(user);

    return token.getId();
  }

  private Map<String, String> getErrors(final BindingResult bindingResult) {
    Map<String, String> errors = new HashMap<>();
    for (FieldError error : bindingResult.getFieldErrors()) {
      errors.put(error.getField(), error.getDefaultMessage());
    }
    return errors;
  }
}
