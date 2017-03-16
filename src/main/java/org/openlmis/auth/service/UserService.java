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
