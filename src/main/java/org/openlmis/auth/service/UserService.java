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


import java.util.Objects;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.dto.UserWithAuthDetailsDto;
import org.openlmis.auth.dto.referencedata.UserDto;
import org.openlmis.auth.repository.UserRepository;
import org.openlmis.auth.service.referencedata.UserReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private EmailVerificationNotifier emailVerificationNotifier;

  private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  /**
   * Creates a new user or updates an existing one.
   *
   * @param request user to be saved.
   * @return saved user.
   */
  public UserWithAuthDetailsDto saveUser(UserWithAuthDetailsDto request) {
    return null == request.getId() ? addUser(request) : updateUser(request);
  }

  private UserWithAuthDetailsDto addUser(UserWithAuthDetailsDto request) {
    request.setVerified(false);

    User dbUser = new User();
    updateUserFields(dbUser, request);

    dbUser = userRepository.save(dbUser);

    request.setId(dbUser.getId());
    UserDto newReferenceDataUser = request.getReferenceDataUser();
    newReferenceDataUser = userReferenceDataService.putUser(newReferenceDataUser);

    sendNotification(dbUser, request);

    return new UserWithAuthDetailsDto(dbUser, newReferenceDataUser);
  }

  private UserWithAuthDetailsDto updateUser(UserWithAuthDetailsDto request) {
    UserDto newReferenceDataUser = request.getReferenceDataUser();
    UserDto oldReferenceDataUser = userReferenceDataService.findOne(request.getId());

    if (!Objects.equals(oldReferenceDataUser.getEmail(), newReferenceDataUser.getEmail())) {
      newReferenceDataUser.setEmail(oldReferenceDataUser.getEmail());
      newReferenceDataUser.setVerified(oldReferenceDataUser.isVerified());

      request.setVerified(false);
    }

    User dbUser = userRepository.findOne(request.getId());
    updateUserFields(dbUser, request);

    dbUser = userRepository.save(dbUser);
    newReferenceDataUser = userReferenceDataService.putUser(newReferenceDataUser);

    sendNotification(dbUser, request);

    return new UserWithAuthDetailsDto(dbUser, newReferenceDataUser);
  }

  private void updateUserFields(User dbUser, UserWithAuthDetailsDto request) {
    dbUser.setUsername(request.getUsername());
    dbUser.setEnabled(request.getEnabled());

    String newPassword = request.getPassword();
    if (StringUtils.hasText(newPassword)) {
      dbUser.setPassword(encoder.encode(newPassword));
    }
  }

  private void sendNotification(User dbUser, UserWithAuthDetailsDto request) {
    if (request.hasEmail() && !request.isVerified()) {
      emailVerificationNotifier.sendNotification(dbUser, request.getEmail());
    }
  }

}
