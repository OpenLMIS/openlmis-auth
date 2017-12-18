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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.openlmis.auth.ApiKeyDataBuilder;
import org.openlmis.auth.domain.ApiKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

public class ApiKeyRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<ApiKey> {

  @Autowired
  private ApiKeyRepository repository;

  @Autowired
  private EntityManager entityManager;

  @Override
  CrudRepository<ApiKey, UUID> getRepository() {
    return repository;
  }

  @Override
  ApiKey generateInstance() {
    return new ApiKeyDataBuilder().buildAsNew();
  }

  @Test(expected = PersistenceException.class)
  public void shouldNotAllowDuplicates() {
    ApiKey key1 = generateInstance();
    ApiKey key2 = new ApiKey(key1.getClientId(), key1.getServiceAccountId());

    repository.save(key1);
    repository.save(key2);

    entityManager.flush();
  }

  @Test
  public void shouldFindByServiceAccountId() {
    ApiKey key = getApiKey();
    Optional<ApiKey> found = repository.findOneByServiceAccountId(key.getServiceAccountId());
    assertThat(found.isPresent(), is(true));
    assertThat(key, is(equalTo(found.get())));
  }

  @Test
  public void shouldNotFindByServiceAccountId() {
    Optional<ApiKey> found = repository.findOneByServiceAccountId(UUID.randomUUID());
    assertThat(found.isPresent(), is(false));
  }

  @Test
  public void shouldFindByClientId() {
    ApiKey key = getApiKey();
    Optional<ApiKey> found = repository.findOneByClientId(key.getClientId());
    assertThat(found.isPresent(), is(true));
    assertThat(key, is(equalTo(found.get())));
  }

  @Test
  public void shouldNotFindByClientId() {
    Optional<ApiKey> found = repository.findOneByClientId(RandomStringUtils.random(5));
    assertThat(found.isPresent(), is(false));
  }

  private ApiKey getApiKey() {
    return repository.save(generateInstance());
  }
}
