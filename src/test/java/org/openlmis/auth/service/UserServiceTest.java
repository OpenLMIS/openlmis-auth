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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.repository.UserRepository;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest extends BaseServiceTest {
  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  @Test
  public void shouldCreateNewUser() {
    // given
    when(userRepository.findOneByReferenceDataUserId(any(UUID.class))).thenReturn(null);
    given(userRepository.save(any(User.class))).willAnswer(new SaveAnswer<User>());

    // when
    User result = userService.saveUser(new User());

    // then
    assertNotNull(result.getId());
  }

  @Test
  public void shouldUpdateExistingUser() {
    // given
    User oldUser = mock(User.class);
    UUID oldUserId = UUID.randomUUID();
    when(oldUser.getId()).thenReturn(oldUserId);

    when(userRepository.findOneByReferenceDataUserId(any(UUID.class))).thenReturn(oldUser);
    given(userRepository.save(any(User.class))).willAnswer(new SaveAnswer<User>());

    // when
    User result = userService.saveUser(new User());

    // then
    assertEquals(oldUserId, result.getId());
  }

  @Test
  public void shouldReplacePasswordDuringUserUpdate() {
    // given
    User oldUser = new User();
    UUID oldUserId = UUID.randomUUID();
    String oldUserPassword = "oldPassword";

    oldUser.setId(oldUserId);
    oldUser.setPassword(oldUserPassword);

    User newUser = mock(User.class);
    when(newUser.getPassword()).thenReturn("newPassword");

    when(userRepository.findOneByReferenceDataUserId(any(UUID.class))).thenReturn(oldUser);
    given(userRepository.save(any(User.class))).willAnswer(new SaveAnswer<User>());

    // when
    User result = userService.saveUser(newUser);

    // then
    assertEquals(oldUserId, result.getId());
    assertNotEquals(oldUserPassword, result.getPassword());
  }

  @Test
  public void shouldNotReplacePasswordWhenNotProvidedDuringUserUpdate() {
    // given
    User oldUser = new User();
    UUID oldUserId = UUID.randomUUID();
    String oldUserPassword = "oldPassword";

    oldUser.setId(oldUserId);
    oldUser.setPassword(oldUserPassword);

    when(userRepository.findOneByReferenceDataUserId(any(UUID.class))).thenReturn(oldUser);
    given(userRepository.save(any(User.class))).willAnswer(new SaveAnswer<User>());

    // when
    User result = userService.saveUser(new User());

    // then
    assertEquals(oldUserId, result.getId());
    assertEquals(oldUserPassword, result.getPassword());
  }
}
