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

public class UnsuccessfulAuthenticationAttemptTest {

  private UnsuccessfulAuthenticationAttempt attempt;

  @Before
  public void setUp() {
    User user = new UserDataBuilder().build();
    attempt = new UnsuccessfulAuthenticationAttempt(user);
  }

  @Test
  public void shouldResetCounter() {
    Random rand = new Random();
    int initialCounter = rand.nextInt(10);
    attempt.setAttemptCounter(initialCounter);
    ZonedDateTime initialAttemptDate = ZonedDateTime.now().minusHours(1L);
    attempt.setLastUnsuccessfulAuthenticationAttemptDate(initialAttemptDate);

    attempt.resetCounter();

    assertThat(attempt.getAttemptCounter()).isZero();
    assertThat(attempt.getLastUnsuccessfulAuthenticationAttemptDate()).isAfter(initialAttemptDate);
  }

  @Test
  public void shouldIncrementCounter() {
    Random rand = new Random();
    int initialCounter = rand.nextInt(10);
    attempt.setAttemptCounter(initialCounter);
    ZonedDateTime initialAttemptDate = ZonedDateTime.now().minusHours(1L);
    attempt.setLastUnsuccessfulAuthenticationAttemptDate(initialAttemptDate);

    attempt.incrementCounter();

    assertThat(attempt.getAttemptCounter() - initialCounter).isOne();
    assertThat(attempt.getLastUnsuccessfulAuthenticationAttemptDate()).isAfter(initialAttemptDate);
  }

}
