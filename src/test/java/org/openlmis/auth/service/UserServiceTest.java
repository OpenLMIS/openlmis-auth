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
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.auth.domain.PasswordResetToken;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.dto.referencedata.UserDto;
import org.openlmis.auth.exception.ValidationMessageException;
import org.openlmis.auth.i18n.ExposedMessageSource;
import org.openlmis.auth.repository.PasswordResetTokenRepository;
import org.openlmis.auth.repository.UserRepository;
import org.openlmis.auth.service.notification.NotificationService;
import org.openlmis.auth.service.referencedata.UserReferenceDataService;

import java.util.Locale;
import java.util.UUID;

@SuppressWarnings({"PMD.UnusedPrivateField"})
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest extends BaseServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PermissionService permissionService;

  @Mock
  private UserReferenceDataService userReferenceDataService;

  @Mock
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @Mock
  private ExposedMessageSource messageSource;

  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private UserService userService;

  @Before
  public void setUp() {
    when(passwordResetTokenRepository.findOneByUser(any(User.class))).thenReturn(null);
    given(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
        .willAnswer(new SaveAnswer<PasswordResetToken>());

    when(messageSource.getMessage(anyString(), any(String[].class), any(Locale.class)))
        .thenReturn(null);
  }

  @Test
  public void shouldCreateNewUser() {
    // given
    when(userReferenceDataService.findOne(any(UUID.class))).thenReturn(new UserDto());
    when(userRepository.findOneByReferenceDataUserId(any(UUID.class))).thenReturn(null);
    given(userRepository.save(any(User.class))).willAnswer(new SaveAnswer<User>());

    // when
    userService.saveUser(new User());

    // then
    verify(userRepository, times(1)).save(any(User.class));
    verifyZeroInteractions(notificationService);
  }

  @Test
  public void shouldUpdateExistingUser() {
    // given
    when(userReferenceDataService.findOne(any(UUID.class))).thenReturn(new UserDto());
    User oldUser = mock(User.class);
    UUID oldUserId = UUID.randomUUID();
    when(oldUser.getUsername()).thenReturn("user");
    when(oldUser.getId()).thenReturn(oldUserId);

    when(userRepository.findOneByReferenceDataUserId(any(UUID.class))).thenReturn(oldUser);
    given(userRepository.save(any(User.class))).willAnswer(new SaveAnswer<User>());

    // when
    userService.saveUser(new User());

    // then
    verify(userRepository, times(1)).save(any(User.class));
    verifyZeroInteractions(notificationService);
  }

  @Test
  public void shouldReplacePasswordDuringUserUpdate() {
    // given
    when(userReferenceDataService.findOne(any(UUID.class))).thenReturn(new UserDto());
    User oldUser = new User();
    UUID oldUserId = UUID.randomUUID();
    String oldUserPassword = "oldPassword";

    oldUser.setId(oldUserId);
    oldUser.setUsername("user");
    oldUser.setPassword(oldUserPassword);

    User newUser = mock(User.class);
    when(newUser.getPassword()).thenReturn("newPassword");

    when(userRepository.findOneByReferenceDataUserId(any(UUID.class))).thenReturn(oldUser);
    ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
    given(userRepository.save(argumentCaptor.capture())).willAnswer(new SaveAnswer<User>());

    // when
    userService.saveUser(newUser);

    // then
    verify(userRepository, times(1)).save(any(User.class));
    verifyZeroInteractions(notificationService);

    User result = argumentCaptor.getValue();
    assertNotEquals(oldUserPassword, result.getPassword());
  }

  @Test
  public void shouldNotReplacePasswordWhenNotProvidedDuringUserUpdate() {
    // given
    when(userReferenceDataService.findOne(any(UUID.class))).thenReturn(new UserDto());
    User oldUser = new User();
    oldUser.setUsername("username");
    UUID oldUserId = UUID.randomUUID();
    String oldUserPassword = "oldPassword";

    oldUser.setId(oldUserId);
    oldUser.setPassword(oldUserPassword);

    when(userRepository.findOneByReferenceDataUserId(any(UUID.class))).thenReturn(oldUser);
    ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
    given(userRepository.save(argumentCaptor.capture())).willAnswer(new SaveAnswer<User>());

    // when
    userService.saveUser(new User());

    // then
    verify(userRepository, times(1)).save(any(User.class));
    verifyZeroInteractions(notificationService);

    User result = argumentCaptor.getValue();
    assertEquals(oldUserPassword, result.getPassword());
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfReferenceDataUserNotFound() throws Exception {
    // given
    when(userReferenceDataService.findOne(any(UUID.class))).thenReturn(null);
    when(userRepository.findOneByReferenceDataUserId(any(UUID.class))).thenReturn(null);
    given(userRepository.save(any(User.class))).willAnswer(new SaveAnswer<User>());

    // when
    userService.saveUser(new User());
  }
}
