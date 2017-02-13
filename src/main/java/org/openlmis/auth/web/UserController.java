
package org.openlmis.auth.web;

import static org.openlmis.auth.service.notification.NotificationRequest.plainTextNotification;

import org.openlmis.auth.domain.PasswordResetToken;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.exception.BindingResultException;
import org.openlmis.auth.exception.ValidationMessageException;
import org.openlmis.auth.i18n.ExposedMessageSource;
import org.openlmis.auth.repository.PasswordResetTokenRepository;
import org.openlmis.auth.repository.UserRepository;
import org.openlmis.auth.service.UserService;
import org.openlmis.auth.service.notification.NotificationService;
import org.openlmis.auth.util.PasswordChangeRequest;
import org.openlmis.util.PasswordResetRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.TokenStore;
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

@RepositoryRestController
@Transactional
public class UserController {
  private static final long RESET_PASSWORD_TOKEN_VALIDITY_HOURS = 12;

  private static final String MAIL_ADDRESS = System.getenv("MAIL_ADDRESS");
  private static final String RESET_PASSWORD_URL =
      System.getenv("BASE_URL") + "/#!/resetPassword/";

  private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private Validator validator;

  @Autowired
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @InitBinder
  protected void initBinder(WebDataBinder binder) {
    binder.setValidator(this.validator);
  }

  @Autowired
  private ExposedMessageSource messageSource;

  @Autowired
  private TokenStore tokenStore;

  @Autowired
  private NotificationService notificationService;

  /**
   * Custom endpoint for creating and updating users. Encrypts password with BCryptPasswordEncoder.
   *
   * @return saved user.
   */
  @RequestMapping(value = "/users/auth", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public User saveUser(@RequestBody User user, BindingResult bindingResult) {
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
        .getMessage("users.logout.confirmation", msgArgs, LocaleContextHolder.getLocale());
  }

  /**
   * Resets a user's password.
   *
   * @return confirmation message or map with field errors.
   */
  @PreAuthorize("hasAuthority('ADMIN')")
  @RequestMapping(value = "/users/auth/passwordReset", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public String passwordReset(
      @RequestBody @Valid PasswordResetRequest passwordResetRequest, BindingResult bindingResult) {
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
            "users.passwordReset.confirmation", msgArgs, LocaleContextHolder.getLocale());
      } else {
        String[] msgArgs = {};
        errors.put("username", messageSource.getMessage("users.passwordReset.userNotFound",
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

    User user = userRepository.findOneByEmail(email);

    if (user == null) {
      throw new ValidationMessageException("users.forgotPassword.userNotFound");
    }

    PasswordResetToken token = createPasswordResetToken(user);

    String[] emailBodyMsgArgs = {user.getUsername(), RESET_PASSWORD_URL + token.getId().toString()};
    String[] emailSubjectMsgArgs = {};

    notificationService.send(plainTextNotification(
        MAIL_ADDRESS,
        email,
        messageSource.getMessage("auth.email.reset-password.subject", emailSubjectMsgArgs,
            LocaleContextHolder.getLocale()),
        messageSource.getMessage("auth.email.reset-password.body", emailBodyMsgArgs,
            LocaleContextHolder.getLocale())));
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
      throw new ValidationMessageException("auth.error.invalidToken");
    }

    if (token.getExpiryDate().isBefore(ZonedDateTime.now())) {
      throw new ValidationMessageException("auth.error.tokenExpired");
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
    User user = userRepository.findOneByReferenceDataUserId(referenceDataUserId);

    PasswordResetToken token = createPasswordResetToken(user);

    return token.getId();
  }

  private PasswordResetToken createPasswordResetToken(User user) {
    PasswordResetToken token = passwordResetTokenRepository.findOneByUser(user);
    if (token != null) {
      passwordResetTokenRepository.delete(token);
    }

    token = new PasswordResetToken();
    token.setUser(user);
    token.setExpiryDate(ZonedDateTime.now().plusHours(RESET_PASSWORD_TOKEN_VALIDITY_HOURS));
    return passwordResetTokenRepository.save(token);
  }

  private Map<String, String> getErrors(final BindingResult bindingResult) {
    return new HashMap<String, String>() {
      {
        for (FieldError error : bindingResult.getFieldErrors()) {
          put(error.getField(), error.getDefaultMessage());
        }
      }
    };
  }
}
