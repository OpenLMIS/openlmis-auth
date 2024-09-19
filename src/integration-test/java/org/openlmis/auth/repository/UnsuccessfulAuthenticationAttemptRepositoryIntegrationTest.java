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

package org.openlmis.auth.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import org.junit.Test;
import org.openlmis.auth.domain.UnsuccessfulAuthenticationAttempt;
import org.openlmis.auth.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

public class UnsuccessfulAuthenticationAttemptRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<UnsuccessfulAuthenticationAttempt> {

  @Autowired
  private UnsuccessfulAuthenticationAttemptRepository unsuccessfulAuthenticationAttemptRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntityManager entityManager;

  @Test
  public void shouldFindAttemptByUserId() throws Exception {
    User user = userRepository.save(generateUser());
    UnsuccessfulAuthenticationAttempt attempt =
        unsuccessfulAuthenticationAttemptRepository.save(generateInstance(user));

    UnsuccessfulAuthenticationAttempt result =
        unsuccessfulAuthenticationAttemptRepository.findByUserId(user.getId()).get();

    assertNotNull(result);
    assertEquals(attempt.getId(), result.getId());
  }

  @Test(expected = PersistenceException.class)
  public void shouldThrowExceptionOnCreatingAttemptsWithSameUser() throws Exception {
    User user = userRepository.save(generateUser());

    unsuccessfulAuthenticationAttemptRepository.save(generateInstance(user));
    unsuccessfulAuthenticationAttemptRepository.save(generateInstance(user));

    entityManager.flush();
  }

  @Override
  CrudRepository<UnsuccessfulAuthenticationAttempt, UUID> getRepository() {
    return unsuccessfulAuthenticationAttemptRepository;
  }

  @Override
  UnsuccessfulAuthenticationAttempt generateInstance() throws Exception {
    return new UnsuccessfulAuthenticationAttempt(userRepository.save(generateUser()));
  }

  UnsuccessfulAuthenticationAttempt generateInstance(User user) throws Exception {
    return new UnsuccessfulAuthenticationAttempt(user);
  }

  private User generateUser() {
    User user = new User();
    user.setUsername("user" + getNextInstanceNumber());
    user.setEnabled(true);
    return user;
  }

}
