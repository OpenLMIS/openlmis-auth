
package org.openlmis.auth.web;

import org.openlmis.auth.domain.Facility;
import org.openlmis.auth.domain.PasswordResetToken;
import org.openlmis.auth.domain.Program;
import org.openlmis.auth.domain.Right;
import org.openlmis.auth.domain.RightQuery;
import org.openlmis.auth.domain.SupervisoryNode;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.i18n.ExposedMessageSource;
import org.openlmis.auth.repository.PasswordResetTokenRepository;
import org.openlmis.auth.repository.UserRepository;
import org.openlmis.auth.util.PasswordChangeRequest;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;

@RepositoryRestController
public class UserController {
  private static final long RESET_PASSWORD_TOKEN_VALIDITY_HOURS = 12;

  private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
  private static final String USER_ID = "userId";

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
  private RestTemplate restTemplate;

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
   * Custom endpoint for creating and updating users. Encrypts password with BCryptPasswordEncoder.
   * @return saved user.
   */
  @RequestMapping(value = "/users", method = RequestMethod.POST)
  public ResponseEntity<?> saveUser(@RequestBody User user, BindingResult bindingResult) {
    LOGGER.debug("Creating or updating user");
    if (bindingResult.getErrorCount() == 0) {
      User dbUser = userRepository.findOneByReferenceDataUserId(user.getReferenceDataUserId());

      if (dbUser != null) {
        dbUser.setUsername(user.getUsername());
        dbUser.setEmail(user.getEmail());
        dbUser.setPassword(user.getPassword());
        dbUser.setEnabled(user.getEnabled());
        dbUser.setRole(user.getRole());
      } else {
        dbUser = user;
      }

      if (dbUser.getPassword() != null && !dbUser.getPassword().isEmpty()) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        dbUser.setPassword(encoder.encode(dbUser.getPassword()));
      } else {
        dbUser.setPassword(null);
      }

      User newUser = userRepository.save(dbUser);

      return new ResponseEntity<>(newUser, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(getErrors(bindingResult), HttpStatus.BAD_REQUEST);
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
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  /**
   * Generates token which can be used to change user's password.
   */
  @RequestMapping(value = "/users/forgotPassword", method = RequestMethod.POST)
  public ResponseEntity<?> forgotPassword(@RequestParam(value = "email") String email) {

    User user = userRepository.findOneByEmail(email);

    if (user == null) {
      return new ResponseEntity<>(messageSource.getMessage("users.forgotPassword.userNotFound",
          null, LocaleContextHolder.getLocale()), HttpStatus.BAD_REQUEST);
    }

    PasswordResetToken token = createPasswordResetToken(user);

    //sendMail(user.getEmail(), token);
    System.out.println("Password reset token: " + token.getId().toString());

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Changes user's password if valid reset token is provided.
   */
  @RequestMapping(value = "/users/changePassword", method = RequestMethod.POST)
  public ResponseEntity<?> showChangePasswordPage(
      @RequestBody PasswordChangeRequest passwordChangeRequest) {

    PasswordResetToken token =
        passwordResetTokenRepository.findOne(passwordChangeRequest.getToken());

    if (token == null) {
      return new ResponseEntity<>("invalid token", HttpStatus.BAD_REQUEST);
    }

    if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
      return new ResponseEntity<>("token expired", HttpStatus.BAD_REQUEST);
    }

    User user = token.getUser();
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    user.setPassword(encoder.encode(passwordChangeRequest.getNewPassword()));
    userRepository.save(user);

    passwordResetTokenRepository.delete(token);

    return new ResponseEntity(HttpStatus.OK);
  }

  /**
   * Creates token which can be used to change user's password.
   */
  @RequestMapping(value = "/users/passwordResetToken", method = RequestMethod.POST)
  public ResponseEntity<?> generatePasswordResetToken(
      @RequestParam(value = "userId") UUID referenceDataUserId) {
    User user = userRepository.findOneByReferenceDataUserId(referenceDataUserId);

    PasswordResetToken token = createPasswordResetToken(user);

    return new ResponseEntity<>(token.getId(), HttpStatus.OK);
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

  private PasswordResetToken createPasswordResetToken(User user) {
    PasswordResetToken token = passwordResetTokenRepository.findOneByUser(user);
    if (token != null) {
      passwordResetTokenRepository.delete(token);
    }

    token = new PasswordResetToken();
    token.setUser(user);
    token.setExpiryDate(LocalDateTime.now().plusHours(RESET_PASSWORD_TOKEN_VALIDITY_HOURS));
    return passwordResetTokenRepository.save(token);
  }


  /**
   * Check if user has a right with certain criteria.
   *
   * @param userId            id of user to check for right
   * @param rightName         right to check
   * @param programCode       program to check
   * @param supervisoryNodeId supervisory node to check
   * @param warehouseCode     warehouse to check
   * @return if successful, true or false depending on if user has the right
   */
  @RequestMapping(value = "/users/{userId}/hasRight", method = RequestMethod.GET)
  public ResponseEntity<?> checkIfUserHasRight(@PathVariable(USER_ID) UUID userId,
                                               @RequestParam(value = "rightName") String rightName,
                                               @RequestParam(value = "programCode",
                                                   required = false) String programCode,
                                               @RequestParam(value = "supervisoryNodeId",
                                                   required = false) UUID supervisoryNodeId,
                                               @RequestParam(value = "warehouseCode",
                                                   required = false) String warehouseCode) {

    User user = restTemplate.getForObject("http://referencedata:8080/api/users/{userId}", User.class, userId);
    if (user == null) {
      LOGGER.error("User not found");
      return ResponseEntity
          .notFound()
          .build();
    }

    RightQuery rightQuery;
    Right right = restTemplate.getForObject("http://referencedata:8080/api/rights/{rightName}", Right.class, 
        rightName);
    if (programCode != null) {

      Program program = restTemplate.getForObject("http://referencedata:8080/api/programs/{programCode}",
          Program.class, programCode);
      if (supervisoryNodeId != null) {

        SupervisoryNode supervisoryNode = restTemplate.getForObject(
            "http://referencedata:8080/api/supervisoryNodes/{supervisoryNodeId}", SupervisoryNode.class, 
                supervisoryNodeId);
        rightQuery = new RightQuery(right, program, supervisoryNode);

      } else {
        rightQuery = new RightQuery(right, program);
      }
    } else if (warehouseCode != null) {

      Facility warehouse = restTemplate.getForObject("http://referencedata:8080/api/facilities/{facilityCode}",
          Facility.class, warehouseCode);
      rightQuery = new RightQuery(right, warehouse);

    } else {
      rightQuery = new RightQuery(right);
    }

    boolean hasRight = user.hasRight(rightQuery);

    return ResponseEntity
        .ok()
        .body(hasRight);
  }

  /**
   * Get the programs at a user's home facility or programs that the user supervises.
   *
   * @param userId          id of user to get programs
   * @param forHomeFacility true to get home facility programs, false to get supervised programs;
   *                        default value is true
   * @return set of programs
   */
  @RequestMapping(value = "/users/{userId}/programs", method = RequestMethod.GET)
  public ResponseEntity<?> getUserPrograms(@PathVariable(USER_ID) UUID userId,
                                           @RequestParam(value = "access_token") String accessToken,
                                           @RequestParam(value = "forHomeFacility",
                                               required = false, defaultValue = "true")
                                               boolean forHomeFacility) {

    User user = restTemplate.getForObject(
        "http://referencedata:8080/api/users/{userId}?access_token={accessToken}", 
        User.class, userId, accessToken);
    if (user == null) {
      LOGGER.error("User not found");
      return ResponseEntity
          .notFound()
          .build();
    }

    Set<Program> programs = forHomeFacility
        ? user.getHomeFacilityPrograms() : user.getSupervisedPrograms();

    return ResponseEntity
        .ok()
        .body(programs);
  }

  /**
   * Get all the facilities that the user supervises.
   *
   * @param userId id of user to get supervised facilities
   * @return set of supervised facilities
   */
  @RequestMapping(value = "/users/{userId}/supervisedFacilities", method = RequestMethod.GET)
  public ResponseEntity<?> getUserSupervisedFacilities(@PathVariable(USER_ID) UUID userId) {

    User user = restTemplate.getForObject("http://referencedata:8080/api/users/{userId}", User.class, userId);
    if (user == null) {
      LOGGER.error("User not found");
      return ResponseEntity
          .notFound()
          .build();
    }

    Set<Facility> supervisedFacilities = user.getSupervisedFacilities();

    return ResponseEntity
        .ok()
        .body(supervisedFacilities);
  }
}
