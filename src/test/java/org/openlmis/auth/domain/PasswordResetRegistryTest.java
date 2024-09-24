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

package org.openlmis.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.auth.UserDataBuilder;

public class PasswordResetRegistryTest {

  private PasswordResetRegistry passwordResetRegistry;

  @Before
  public void setUp() {
    User user = new UserDataBuilder().build();
    passwordResetRegistry = new PasswordResetRegistry(user);
  }

  @Test
  public void shouldResetCounter() {
    Random rand = new Random();
    int initialCounter = rand.nextInt(10);
    passwordResetRegistry.setAttemptCounter(initialCounter);
    ZonedDateTime initialLastCounterResetDate = ZonedDateTime.now().minusDays(1L);
    passwordResetRegistry.setLastCounterResetDate(initialLastCounterResetDate);

    passwordResetRegistry.resetCounter();

    assertThat(passwordResetRegistry.getAttemptCounter()).isZero();
    assertThat(passwordResetRegistry.getLastCounterResetDate())
        .isAfter(initialLastCounterResetDate);
  }

  @Test
  public void shouldIncrementCounter() {
    Random rand = new Random();
    int initialCounter = rand.nextInt(10);
    passwordResetRegistry.setAttemptCounter(initialCounter);
    ZonedDateTime initialLastAttemptDate = ZonedDateTime.now().minusHours(1L);
    passwordResetRegistry.setLastAttemptDate(initialLastAttemptDate);

    passwordResetRegistry.incrementCounter();

    assertThat(passwordResetRegistry.getAttemptCounter() - initialCounter).isOne();
    assertThat(passwordResetRegistry.getLastAttemptDate()).isAfter(initialLastAttemptDate);
  }

}