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

package org.openlmis.auth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.openlmis.auth.TestDataInitializer.AUTH_USERS_TABLE;
import static org.openlmis.auth.TestDataInitializer.OAUTH_CLIENT_DETAILS_TABLE;
import static org.openlmis.auth.TestDataInitializer.API_KEYS_TABLE;

@RunWith(MockitoJUnitRunner.class)
public class TestDataInitializerTest {

  @Mock
  private Resource apiKeysResource;

  @Mock
  private Resource authUsersResource;

  @Mock
  private Resource oauthClientDetailsResource;

  @Mock
  private Resource2Db loader;

  @InjectMocks
  private TestDataInitializer initializer = new TestDataInitializer(loader);

  @Test
  public void shouldLoadData() throws IOException {
    initializer.run();

    verify(loader).insertToDbFromCsv(API_KEYS_TABLE, apiKeysResource);
    verify(loader).insertToDbFromCsv(AUTH_USERS_TABLE, authUsersResource);
    verify(loader).insertToDbFromCsv(OAUTH_CLIENT_DETAILS_TABLE, oauthClientDetailsResource);
  }
}
