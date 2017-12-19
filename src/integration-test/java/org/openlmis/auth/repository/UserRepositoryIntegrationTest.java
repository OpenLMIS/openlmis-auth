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

import org.junit.Test;
import org.openlmis.auth.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

public class UserRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<User> {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntityManager entityManager;

  @Test
  public void shouldFindUsersByUsernameIgnoringCase() throws Exception {
    User user = generateInstance();
    userRepository.save(user);

    String searchTerm = user.getUsername().toUpperCase();
    Optional<User> found = userRepository.findOneByUsernameIgnoreCase(searchTerm);

    assertNotNull(found.get());
    assertEquals(user.getUsername(), found.get().getUsername());
  }

  @Test(expected = PersistenceException.class)
  public void shouldThrowExceptionOnCreatingSameUsernameWithDifferentCasing() throws Exception {
    User user = generateInstance();
    userRepository.save(user);

    User newUser = generateInstance();
    newUser.setUsername(user.getUsername().toUpperCase());
    userRepository.save(newUser);

    entityManager.flush();
  }

  @Override
  CrudRepository<User, UUID> getRepository() {
    return userRepository;
  }

  @Override
  User generateInstance() throws Exception {
    User user = new User();
    user.setReferenceDataUserId(UUID.randomUUID());
    user.setUsername("user" + getNextInstanceNumber());
    user.setEnabled(true);
    return user;
  }
}
