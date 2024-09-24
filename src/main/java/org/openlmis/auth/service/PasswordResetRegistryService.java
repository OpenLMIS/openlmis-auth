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

import static org.openlmis.auth.i18n.MessageKeys.ERROR_TOO_MANY_REQUESTS;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.openlmis.auth.domain.PasswordResetRegistry;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.exception.TooManyRequestsException;
import org.openlmis.auth.repository.PasswordResetRegistryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetRegistryService {

  @Value("${password.reset.maxAttempts}")
  private int maxAttempt;

  @Value("${password.reset.maxTimeForAttempts}")
  private long maxTimeForAttempts;

  @Value("${password.reset.lockoutTime}")
  private long lockoutTime;

  @Autowired
  private PasswordResetRegistryRepository passwordResetRegistryRepository;

  /**
   * Checks whether the user has exceeded the limit of attempts to send a password reset request.
   *
   * @param user @param user the User attempting a password reset
   */
  public void checkPasswordResetLimit(User user) {
    Optional<PasswordResetRegistry> registryOpt = passwordResetRegistryRepository.findByUser(user);
    ZonedDateTime now = ZonedDateTime.now();

    PasswordResetRegistry registry;
    if (registryOpt.isPresent()) {
      registry = registryOpt.get();

      if (Boolean.TRUE.equals(registry.getBlocked())) {
        long secondsSinceLastAttempt =
            Duration.between(registry.getLastAttemptDate(), now).getSeconds();
        if (secondsSinceLastAttempt < lockoutTime) {
          throw new TooManyRequestsException(ERROR_TOO_MANY_REQUESTS);
        } else {
          registry.resetCounter();
          //registry.setAttemptCounter(0);
          //registry.setBlocked(false);
          //registry.setLastCounterResetDate(now);
        }
      }

      long secondsSinceFirstAttempt =
          Duration.between(registry.getLastCounterResetDate(), now).getSeconds();
      if (secondsSinceFirstAttempt > maxTimeForAttempts) {
        registry.setAttemptCounter(0);
        registry.setLastCounterResetDate(now);
      }

      registry.incrementCounter();
      //registry.setAttemptCounter(registry.getAttemptCounter() + 1);
      //registry.setLastAttemptDate(now);

      if (registry.getAttemptCounter() >= maxAttempt) {
        registry.setBlocked(true);
      }
    } else {
      PasswordResetRegistry newRegistry = new PasswordResetRegistry(user);
      newRegistry.setAttemptCounter(1);
      newRegistry.setLastAttemptDate(now);
      newRegistry.setLastCounterResetDate(now);

      registry = newRegistry;
    }
    passwordResetRegistryRepository.save(registry);
  }

}
