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
import org.openlmis.auth.domain.PasswordResetRegistry;
import org.openlmis.auth.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

public class PasswordResetRegistryRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<PasswordResetRegistry> {

  @Autowired
  private PasswordResetRegistryRepository passwordResetRegistryRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntityManager entityManager;

  @Test
  public void shouldFindRegistryByUser() throws Exception {
    User user = userRepository.save(generateUser());
    PasswordResetRegistry registry = passwordResetRegistryRepository.save(generateInstance(user));

    PasswordResetRegistry result = passwordResetRegistryRepository.findByUser(user).get();

    assertNotNull(result);
    assertEquals(registry.getId(), result.getId());
  }

  @Test(expected = PersistenceException.class)
  public void shouldThrowExceptionOnCreatingRegistryWithSameUser() throws Exception {
    User user = userRepository.save(generateUser());

    passwordResetRegistryRepository.save(generateInstance(user));
    passwordResetRegistryRepository.save(generateInstance(user));

    entityManager.flush();
  }

  @Override
  CrudRepository<PasswordResetRegistry, UUID> getRepository() {
    return passwordResetRegistryRepository;
  }

  @Override
  PasswordResetRegistry generateInstance() throws Exception {
    return new PasswordResetRegistry(userRepository.save(generateUser()));
  }

  PasswordResetRegistry generateInstance(User user) throws Exception {
    return new PasswordResetRegistry(user);
  }

  private User generateUser() {
    User user = new User();
    user.setUsername("user" + getNextInstanceNumber());
    user.setEnabled(true);
    return user;
  }

}
