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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import org.openlmis.auth.domain.UnsuccessfulAuthenticationAttempt;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.dto.UnlockResponseDto;
import org.openlmis.auth.dto.UserDto;
import org.openlmis.auth.repository.UnsuccessfulAuthenticationAttemptRepository;
import org.openlmis.auth.repository.UserRepository;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UnsuccessfulAuthenticationAttemptRepository attemptCounterRepository;

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

  @Test
  public void shouldFindAllAuthUsers() {
    when(userRepository.findAll()).thenReturn(Arrays.asList(new User(), new User()));

    List<User> result = userService.findAll();

    assertEquals(2, result.size());
    verify(userRepository).findAll();
  }

  @Test
  public void shouldUnlockUserAndResetCounter() {
    User user = new UserDataBuilder().asLockedOut(true).build();
    UnsuccessfulAuthenticationAttempt counter = new UnsuccessfulAuthenticationAttempt(user);
    counter.incrementCounter();

    when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
    when(attemptCounterRepository.findByUserId(user.getId())).thenReturn(Optional.of(counter));

    String username = userService.unlockUser(user.getId());

    assertEquals(user.getUsername(), username);
    assertFalse(user.isLockedOut());
    assertEquals(Integer.valueOf(0), counter.getAttemptCounter());
    verify(userRepository).findByIdForUpdate(user.getId());
    verify(attemptCounterRepository).save(counter);
    verify(userRepository).save(user);
  }

  @Test
  public void shouldUnlockUserWithoutCounterRow() {
    User user = new UserDataBuilder().asLockedOut(true).build();

    when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
    when(attemptCounterRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

    String username = userService.unlockUser(user.getId());

    assertEquals(user.getUsername(), username);
    assertFalse(user.isLockedOut());
    verify(attemptCounterRepository, never()).save(any(UnsuccessfulAuthenticationAttempt.class));
    verify(userRepository).save(user);
  }

  @Test
  public void shouldReturnNullWhenUnlockingMissingUser() {
    UUID userId = UUID.randomUUID();
    when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.empty());

    String username = userService.unlockUser(userId);

    assertNull(username);
    verify(userRepository, never()).save(any(User.class));
    verify(attemptCounterRepository, never()).save(any(UnsuccessfulAuthenticationAttempt.class));
  }

  @Test
  public void shouldGetAuthUsersFilteredByLockoutState() {
    User locked = new UserDataBuilder().asLockedOut(true).build();
    User unlocked = new UserDataBuilder().asLockedOut(false).build();
    when(userRepository.findAll()).thenReturn(Arrays.asList(locked, unlocked));

    List<UserDto> all = userService.getAuthUsers(null);
    assertEquals(2, all.size());
    assertTrue(all.stream().allMatch(dto -> dto.getPassword() == null));

    List<UserDto> lockedOnly = userService.getAuthUsers(true);
    assertEquals(1, lockedOnly.size());
    assertEquals(locked.getId(), lockedOnly.get(0).getId());
    assertTrue(lockedOnly.get(0).isLockedOut());

    List<UserDto> unlockedOnly = userService.getAuthUsers(false);
    assertEquals(1, unlockedOnly.size());
    assertEquals(unlocked.getId(), unlockedOnly.get(0).getId());
  }

  @Test
  public void shouldBulkUnlockUsersGroupingResults() {
    ReflectionTestUtils.setField(userService, "self", userService);

    User user = new UserDataBuilder().asLockedOut(true).build();
    UUID missingId = UUID.randomUUID();
    when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
    when(userRepository.findByIdForUpdate(missingId)).thenReturn(Optional.empty());
    when(attemptCounterRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

    UnlockResponseDto response =
        userService.unlockUsers(Arrays.asList(user.getId(), missingId), "admin");

    assertEquals(Collections.singletonList(user.getId()), response.getUnlocked());
    assertEquals(Collections.singletonList(missingId), response.getNotFound());
    assertTrue(response.getFailed().isEmpty());
    assertFalse(user.isLockedOut());
  }
}
