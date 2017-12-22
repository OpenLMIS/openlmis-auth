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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.openlmis.auth.ApiKeyDataBuilder;
import org.openlmis.auth.domain.ApiKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public class ApiKeyRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<ApiKey> {

  @Autowired
  private ApiKeyRepository repository;

  @Override
  CrudRepository<ApiKey, UUID> getRepository() {
    return repository;
  }

  @Override
  ApiKey generateInstance() {
    return new ApiKeyDataBuilder().build();
  }

  @Override
  protected void assertBefore(ApiKey instance) {
    assertThat(instance.getToken(), is(notNullValue()));
  }

}
