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

package org.openlmis.auth.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.internal.Objects;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.auth.UserDataBuilder;
import org.openlmis.auth.domain.UnsuccessfulAuthenticationAttempt;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.repository.UnsuccessfulAuthenticationAttemptRepository;
import org.openlmis.auth.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class OlmisAuthenticationProviderTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UnsuccessfulAuthenticationAttemptRepository unsuccessfulAuthenticationAttemptRepository;

  @Mock
  private UserDetails userDetails;

  @Mock
  private UsernamePasswordAuthenticationToken authentication;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private OlmisAuthenticationProvider olmisAuthenticationProvider;

  private User user;
  private UnsuccessfulAuthenticationAttempt attempt;

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(olmisAuthenticationProvider, "maxUnsuccessfulAuthAttempts", 3);
    ReflectionTestUtils.setField(olmisAuthenticationProvider, "lockoutTime", 60);

    user = new UserDataBuilder().build();
    attempt = new UnsuccessfulAuthenticationAttempt(user);

    when(userRepository.findOneByUsernameIgnoreCase(anyString())).thenReturn(user);
    when(userDetails.getUsername()).thenReturn(user.getUsername());
    when(unsuccessfulAuthenticationAttemptRepository.findByUserId(any(UUID.class)))
        .thenReturn(Optional.of(attempt));
  }

  @Test(expected = LockedException.class)
  public void shouldThrowExceptionIfUserLockedOutAndLockoutNotExpired() {
    // given
    user.setLockedOut(true);
    attempt.setLastUnsuccessfulAuthenticationAttemptDate(ZonedDateTime.now().minusSeconds(30));

    // when
    olmisAuthenticationProvider.additionalAuthenticationChecks(userDetails, authentication);
  }

  @Test(expected = BadCredentialsException.class)
  public void shouldThrowExceptionIfAuthenticationFail() {
    // given
    doThrow(new BadCredentialsException("Bad credentials")).when(authentication).getCredentials();

    // when
    olmisAuthenticationProvider.additionalAuthenticationChecks(userDetails, authentication);
  }

  @Test
  public void shouldUnlockLockedOutUserIfLockoutExpired() {
    // given
    when(authentication.getCredentials()).thenReturn(Objects.instance());
    when(userDetails.getPassword()).thenReturn("test-password");
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(Boolean.TRUE);
    user.setLockedOut(true);
    attempt.setLastUnsuccessfulAuthenticationAttemptDate(ZonedDateTime.now().minusSeconds(61));

    // when
    olmisAuthenticationProvider.additionalAuthenticationChecks(userDetails, authentication);

    // then
    assertFalse(user.isLockedOut());
    verify(userRepository).save(user);
    verify(unsuccessfulAuthenticationAttemptRepository).save(attempt);
  }

  @Test
  public void shouldLockOutUserIfExceedsMaxAttemptNumber() {
    // given
    user.setLockedOut(false);
    attempt.setAttemptCounter(2);
    doThrow(new BadCredentialsException("Bad credentials")).when(authentication).getCredentials();

    // when
    BadCredentialsException exception =
        assertThrows(BadCredentialsException.class, () ->
            olmisAuthenticationProvider.additionalAuthenticationChecks(userDetails, authentication)
        );

    // then
    assertNotNull(exception);
    assertTrue(user.isLockedOut());
    verify(userRepository).save(user);
    verify(unsuccessfulAuthenticationAttemptRepository).save(attempt);
  }

}
