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

import java.time.Duration;
import java.time.ZonedDateTime;
import org.openlmis.auth.domain.UnsuccessfulAuthenticationAttempt;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.repository.UnsuccessfulAuthenticationAttemptRepository;
import org.openlmis.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

public class OlmisAuthenticationProvider extends DaoAuthenticationProvider {

  @Value("${maxUnsuccessfulAuthAttempts}")
  private int maxUnsuccessfulAuthAttempts;

  @Value("${lockoutTime}")
  private long lockoutTime;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UnsuccessfulAuthenticationAttemptRepository attemptCounterRepository;

  /**
   * Wraps the attempt in one transaction so the lock taken below is held across the whole
   * lockout-state read-modify-write. noRollbackFor is required because a failed login throws an
   * AuthenticationException after incrementing the counter - without it the counter (and lockout)
   * would be rolled back. Adds locking only; does not change the lockout logic.
   */
  @Override
  @Transactional(noRollbackFor = AuthenticationException.class)
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    return super.authenticate(authentication);
  }

  @Override
  protected void additionalAuthenticationChecks(UserDetails userDetails,
      UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
    User user = userRepository.findOneByUsernameIgnoreCase(userDetails.getUsername());
    // pessimistic write lock to serialize with a concurrent administrative unlock of this user
    user = userRepository.findByIdForUpdate(user.getId()).orElse(user);
    UnsuccessfulAuthenticationAttempt counter = attemptCounterRepository
        .findByUserId(user.getId())
        .orElse(new UnsuccessfulAuthenticationAttempt(user));
    boolean lockoutExpired =
        Duration.between(counter.getLastUnsuccessfulAuthenticationAttemptDate(),
            ZonedDateTime.now()).getSeconds() > lockoutTime;

    if (user.isLockedOut() && !lockoutExpired) {
      throw new LockedException("Too many failed login attempts. "
          + "You can't access this page right now. Please try again later.");
    } else if (user.isLockedOut()) {
      user.setLockedOut(false);
      counter.resetCounter();
      attemptCounterRepository.save(counter);
      userRepository.save(user);
    }

    try {
      super.additionalAuthenticationChecks(userDetails, authentication);
    } catch (Exception ex) {
      counter.incrementCounter();

      if (counter.getAttemptCounter() >= maxUnsuccessfulAuthAttempts) {
        user.setLockedOut(true);
        userRepository.save(user);
      }
      attemptCounterRepository.save(counter);
      throw ex;
    }

  }

}
