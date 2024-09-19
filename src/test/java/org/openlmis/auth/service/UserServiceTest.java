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

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.auth.SaveAnswer;
import org.openlmis.auth.UserDataBuilder;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.dto.UserDto;
import org.openlmis.auth.repository.UserRepository;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  @Before
  public void setUp() {
    given(userRepository.save(any(User.class))).willAnswer(new SaveAnswer<>());
  }

  @Test
  public void shouldCreateNewUser() {
    // given
    when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

    // when
    User user = new UserDataBuilder().build();

    UserDto request = new UserDto();
    user.export(request);

    userService.saveUser(request);

    // then
    verify(userRepository).save(any(User.class));
  }

  @Test
  public void shouldUpdateExistingUser() {
    // given
    User oldUser = mock(User.class);
    UUID oldUserId = UUID.randomUUID();
    when(oldUser.getId()).thenReturn(oldUserId);

    when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(oldUser));

    // when
    User user = new UserDataBuilder().build();

    UserDto request = new UserDto();
    user.export(request);

    userService.saveUser(request);

    // then
    verify(userRepository).save(any(User.class));
    verify(oldUser).updateFrom(request);
  }

}
