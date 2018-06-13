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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.auth.DummyUserDto;
import org.openlmis.auth.SaveAnswer;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.dto.UserWithAuthDetailsDto;
import org.openlmis.auth.dto.referencedata.UserDto;
import org.openlmis.auth.repository.UserRepository;
import org.openlmis.auth.service.referencedata.UserReferenceDataService;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserReferenceDataService userReferenceDataService;

  @Mock
  private EmailVerificationNotifier emailVerificationNotifier;

  @InjectMocks
  private UserService userService;

  @Captor
  private ArgumentCaptor<UserDto> userCaptor;

  private DummyUserDto referenceDataUser = new DummyUserDto();

  @Before
  public void setUp() {
    when(userReferenceDataService.findOne(any(UUID.class))).thenReturn(referenceDataUser);
    when(userReferenceDataService.putUser(any(UserDto.class))).thenReturn(referenceDataUser);
  }

  @Test
  public void shouldCreateNewUser() {
    // given
    when(userRepository.findOne(any(UUID.class))).thenReturn(null);
    given(userRepository.save(any(User.class))).willAnswer(new SaveAnswer<User>());

    // when
    UserWithAuthDetailsDto request = new UserWithAuthDetailsDto(new User(), referenceDataUser);
    request.setId(null);

    userService.saveUser(request);

    // then
    verify(userRepository).save(any(User.class));
    verify(emailVerificationNotifier)
        .sendNotification(any(User.class), eq(referenceDataUser.getEmail()));
  }

  @Test
  public void shouldUpdateExistingUser() {
    // given
    User oldUser = mock(User.class);
    UUID oldUserId = UUID.randomUUID();
    when(oldUser.getUsername()).thenReturn("user");
    when(oldUser.getId()).thenReturn(oldUserId);

    when(userRepository.findOne(any(UUID.class))).thenReturn(oldUser);
    given(userRepository.save(any(User.class))).willAnswer(new SaveAnswer<User>());

    // when
    userService.saveUser(new UserWithAuthDetailsDto(new User(), referenceDataUser));

    // then
    verify(userRepository, times(1)).save(any(User.class));
    verifyZeroInteractions(emailVerificationNotifier);
  }

  @Test
  public void shouldReplaceEmailAndVerificationFlagIfEmailWasChanged() {
    // given
    when(userRepository.findOne(any(UUID.class))).thenReturn(new User());
    given(userRepository.save(any(User.class))).willAnswer(new SaveAnswer<User>());

    // when
    UserWithAuthDetailsDto request = new UserWithAuthDetailsDto(new User(), referenceDataUser);
    request.setEmail("my_test_email@unit.test.org");

    userService.saveUser(request);

    // then
    verify(userReferenceDataService).putUser(userCaptor.capture());
    verify(userRepository).save(any(User.class));
    verify(emailVerificationNotifier).sendNotification(any(User.class), eq(request.getEmail()));

    UserDto user = userCaptor.getValue();
    assertThat(user.getEmail()).isEqualTo(referenceDataUser.getEmail());
    assertThat(user.isVerified()).isEqualTo(referenceDataUser.isVerified());
  }

  @Test
  public void shouldReplacePasswordDuringUserUpdate() {
    // given
    User oldUser = new User();
    UUID oldUserId = UUID.randomUUID();
    String oldUserPassword = "oldPassword";

    oldUser.setId(oldUserId);
    oldUser.setUsername("user");
    oldUser.setPassword(oldUserPassword);

    User newUser = mock(User.class);
    when(newUser.getPassword()).thenReturn("newPassword");

    when(userRepository.findOne(any(UUID.class))).thenReturn(oldUser);
    ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
    given(userRepository.save(argumentCaptor.capture())).willAnswer(new SaveAnswer<User>());

    // when
    userService.saveUser(new UserWithAuthDetailsDto(newUser, referenceDataUser));

    // then
    verify(userRepository, times(1)).save(any(User.class));
    verifyZeroInteractions(emailVerificationNotifier);

    User result = argumentCaptor.getValue();
    assertThat(result.getPassword()).isNotEqualTo(oldUserPassword);
  }

  @Test
  public void shouldNotReplacePasswordWhenNotProvidedDuringUserUpdate() {
    // given
    UUID oldUserId = UUID.randomUUID();
    String oldUserPassword = "oldPassword";

    User oldUser = new User();
    oldUser.setUsername("username");
    oldUser.setId(oldUserId);
    oldUser.setPassword(oldUserPassword);

    when(userRepository.findOne(any(UUID.class))).thenReturn(oldUser);
    ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
    given(userRepository.save(argumentCaptor.capture())).willAnswer(new SaveAnswer<User>());

    // when
    userService.saveUser(new UserWithAuthDetailsDto(new User(), referenceDataUser));

    // then
    verify(userRepository, times(1)).save(any(User.class));
    verifyZeroInteractions(emailVerificationNotifier);

    User result = argumentCaptor.getValue();
    assertThat(result.getPassword()).isEqualTo(oldUserPassword);
  }

}
