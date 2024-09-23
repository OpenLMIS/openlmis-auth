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

import java.util.Optional;
import org.openlmis.auth.domain.PasswordResetRegistry;
import org.openlmis.auth.domain.User;
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

  public void checkPasswordResetAttemptLimit(User user) {
    Optional<PasswordResetRegistry> attemptOpt = passwordResetRegistryRepository.findByUser(user);
    if (attemptOpt.isPresent()) {
      PasswordResetRegistry attempt = attemptOpt.get();

      /*
      * is locked out?
      * if yes, check if still should be (if time between new() and last attempt is more or less tha lockout time
      *   if still should be, throw exception
      *   if shouldn't be anymore, delete the registry
      * is not locked out?
      * increment counter
      * Now, we have that mechanism, that if user tries to reset password too many times in specific time range (maxTimeForAttempts)
      * we have to block him.
      * So, after first attempt (createdDate in entity) he can't try more than 'maxAttempt' in 'maxTimeForAttempts' time.
      * If time between 'createdDate' in PasswordResetRegistry and now() is bigger than maxTimeForAttempts i think we should
      * reset it, i mean, we have to count down the time again, so maybe delete attempt?
      * If he didn't cross the maxAttempts, just return
      * if he did cross the maxAttempts and this time range is less than maxTimeForAttempts, also throw the same exception as above
      *
      * */
    }
    // create new or something?
  }

}
