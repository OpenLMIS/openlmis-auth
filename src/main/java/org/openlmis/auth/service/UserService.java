package org.openlmis.auth.service;


import org.openlmis.auth.domain.User;
import org.openlmis.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  /**
   * Creates a new user or updates an existing one.
   *
   * @param user user to be saved.
   * @return saved user.
   */
  public User saveUser(User user) {
    User dbUser = userRepository.findOneByReferenceDataUserId(user.getReferenceDataUserId());

    if (dbUser != null) {
      dbUser.setUsername(user.getUsername());
      dbUser.setEmail(user.getEmail());
      dbUser.setEnabled(user.getEnabled());
      dbUser.setRole(user.getRole());
    } else {
      dbUser = user;
    }

    String newPassword = user.getPassword();
    if (StringUtils.hasText(newPassword)) {
      BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
      dbUser.setPassword(encoder.encode(newPassword));
    }

    return userRepository.save(dbUser);
  }
}
