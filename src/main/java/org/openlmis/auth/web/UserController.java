
package org.openlmis.auth.web;

import org.openlmis.auth.domain.User;
import org.openlmis.auth.i18n.ExposedMessageSource;
import org.openlmis.auth.repository.UserRepository;
import org.openlmis.auth.util.PasswordResetRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.support.SessionStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;

@RepositoryRestController
public class UserController {
  private Logger logger = LoggerFactory.getLogger(UserController.class);

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private Validator validator;

  @InitBinder
  protected void initBinder(WebDataBinder binder) {
    binder.setValidator(this.validator);
  }

  @Autowired
  private ExposedMessageSource messageSource;

  @Autowired
  private TokenStore tokenStore;

  /**
   * Endpoint for logout.
   */
  @PreAuthorize("isAuthenticated()")
  @RequestMapping(value = "/users/logout", method = RequestMethod.POST)
  public ResponseEntity<?> revokeToken(OAuth2Authentication auth) {
    OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();
    String token = details.getTokenValue();
    tokenStore.removeAccessToken(new DefaultOAuth2AccessToken(token));

    String[] msgArgs = {};
    return new ResponseEntity<String>(messageSource.getMessage(
        "users.logout.confirmation", msgArgs, LocaleContextHolder.getLocale()), HttpStatus.OK);
  }

  /**
   * Custom endpoint for creating new users. Encrypts password with BCryptPasswordEncoder.
   * @return newly created user.
   */
  @RequestMapping(value = "/users", method = RequestMethod.POST)
  public ResponseEntity<?> createUser(@RequestBody User user,
                                      BindingResult bindingResult, SessionStatus status) {
    logger.debug("Creating new user");
    if (bindingResult.getErrorCount() == 0) {
      BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
      user.setPassword(encoder.encode(user.getPassword()));
      User newUser = userRepository.save(user);

      return new ResponseEntity<User>(newUser, HttpStatus.CREATED);
    } else {
      return new ResponseEntity(getErrors(bindingResult), HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Resets a user's password.
   * @return confirmation message or map with field errors.
   */
  @PreAuthorize("hasAuthority('ADMIN')")
  @RequestMapping(value = "/users/passwordReset", method = RequestMethod.POST)
  public ResponseEntity<?> passwordReset(
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
        return new ResponseEntity<String>(messageSource.getMessage(
            "users.passwordReset.confirmation", msgArgs, LocaleContextHolder.getLocale()),
            HttpStatus.OK);
      } else {
        String[] msgArgs = {};
        errors.put("username", messageSource.getMessage("users.passwordReset.userNotFound",
            msgArgs, LocaleContextHolder.getLocale()));
      }
    } else {
      errors.putAll(getErrors(bindingResult));
    }
    return new ResponseEntity(errors, HttpStatus.BAD_REQUEST);
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
