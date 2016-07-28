
package org.openlmis.auth.web;

import org.openlmis.auth.domain.User;
import org.openlmis.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.support.SessionStatus;

import java.util.HashMap;
import java.util.Map;

@RepositoryRestController
public class UserController {
  private Logger logger = LoggerFactory.getLogger(UserController.class);

  @Autowired
  private UserRepository userRepository;

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
      return new ResponseEntity(getUserErrors(bindingResult), HttpStatus.BAD_REQUEST);
    }
  }

  private Map<String, String> getUserErrors(final BindingResult bindingResult) {
    return new HashMap<String, String>() {
      {
        for (FieldError error : bindingResult.getFieldErrors()) {
          put(error.getField(), error.getDefaultMessage());
        }
      }
    };
  }
}
