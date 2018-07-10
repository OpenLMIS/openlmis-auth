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
import org.openlmis.auth.dto.UserDto;
import org.openlmis.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  /**
   * Creates a new user or updates an existing one.
   *
   * @param request user to be saved.
   * @return saved user.
   */
  public UserDto saveUser(UserDto request) {
    return null == request.getId() ? addUser(request) : updateUser(request);
  }

  private UserDto addUser(UserDto request) {
    User dbUser = User.newInstance(request);
    dbUser = userRepository.save(dbUser);

    return toDto(dbUser, request);
  }

  private UserDto updateUser(UserDto request) {
    User dbUser = userRepository.findOne(request.getId());
    dbUser.updateFrom(request);

    dbUser = userRepository.save(dbUser);

    return toDto(dbUser, request);
  }

  private UserDto toDto(User user, UserDto request) {
    UserDto response = new UserDto();
    user.export(response);
    response.setPassword(request.getPassword());

    return response;
  }

}
