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
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.auth.domain.PasswordResetRegistry;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.exception.TooManyRequestsMessageException;
import org.openlmis.auth.repository.PasswordResetRegistryRepository;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PasswordResetRegistryServiceTest {

  @Mock
  private PasswordResetRegistryRepository passwordResetRegistryRepository;

  @Mock
  private User user;

  private PasswordResetRegistry registry;

  @InjectMocks
  private PasswordResetRegistryService passwordResetRegistryService;

  @Before
  public void setUp() {
    registry = new PasswordResetRegistry(user);

    ReflectionTestUtils.setField(passwordResetRegistryService, "maxAttempt", 3);
    ReflectionTestUtils.setField(passwordResetRegistryService, "maxTimeForAttempts", 60);
    ReflectionTestUtils.setField(passwordResetRegistryService, "lockoutTime", 120);

    when(passwordResetRegistryRepository.findByUser(any(User.class)))
        .thenReturn(Optional.of(registry));
  }

  @Test
  public void shouldThrowTooManyRequestsExceptionIfBlockedAndLockoutNotExpired() {
    // given
    registry.setBlocked(true);
    registry.setLastAttemptDate(ZonedDateTime.now().minusSeconds(30));

    // then
    assertThrows(TooManyRequestsMessageException.class, () -> {
      passwordResetRegistryService.checkPasswordResetLimit(user);
    });
  }

  @Test
  public void shouldResetCounterIfLockoutExpired() {
    // given
    registry.setBlocked(true);
    registry.setLastAttemptDate(ZonedDateTime.now().minusSeconds(150));

    // when
    passwordResetRegistryService.checkPasswordResetLimit(user);

    // then
    assertFalse(registry.getBlocked());
    verify(passwordResetRegistryRepository).save(registry);
  }

  @Test
  public void shouldResetCounterIfTimeExceededForAttempts() {
    // given
    registry.setAttemptCounter(2);
    registry.setLastCounterResetDate(ZonedDateTime.now().minusSeconds(90));

    // when
    passwordResetRegistryService.checkPasswordResetLimit(user);

    // then
    assertEquals(1, (int) registry.getAttemptCounter());
    verify(passwordResetRegistryRepository).save(registry);
  }

  @Test
  public void shouldBlockUserIfExceedsMaxAttempts() {
    // given
    registry.setAttemptCounter(2);

    // when
    passwordResetRegistryService.checkPasswordResetLimit(user);

    // then
    assertTrue(registry.getBlocked());
    verify(passwordResetRegistryRepository).save(registry);
  }

  @Test
  public void shouldCreateNewRegistryIfNotExists() {
    // given
    when(passwordResetRegistryRepository.findByUser(any(User.class))).thenReturn(Optional.empty());

    // when
    passwordResetRegistryService.checkPasswordResetLimit(user);

    // then
    verify(passwordResetRegistryRepository, times(1)).save(any(PasswordResetRegistry.class));
  }
}