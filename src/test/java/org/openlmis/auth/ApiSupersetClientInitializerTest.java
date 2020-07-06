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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.auth.domain.Client;
import org.openlmis.auth.repository.ClientRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = ApiSupersetClientInitializer.class)
@TestPropertySource
public class ApiSupersetClientInitializerTest {

  @InjectMocks
  private ApiSupersetClientInitializer creator;

  @Mock
  private ClientRepository clientRepository;

  private static final String CLIENT_ID = "superset";
  private static final String CLIENT_SECRET = "secret";
  private static final String CLIENT_REDIRECT_URI = "https://superset-uat.openlmis.org/oauth-authorized/openlmis";

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(creator, "supersetClientId", CLIENT_ID);
    ReflectionTestUtils.setField(creator, "supersetClientSecret", CLIENT_SECRET);
    ReflectionTestUtils.setField(creator, "supersetClientRedirectUri", CLIENT_REDIRECT_URI);
  }

  @Test
  public void shouldDoNothingIfPropertiesAreNotDefined() {
    ReflectionTestUtils.setField(creator, "supersetClientId", null);
    ReflectionTestUtils.setField(creator, "supersetClientSecret", null);

    creator.run();

    verifyNoInteractions(clientRepository);
  }

  @Test
  public void shouldNotUpdateIfClientAlreadyExists() {
    Client client = new ClientDataBuilder().buildSupersetClient();
    client.setClientSecret(CLIENT_SECRET);
    client.setRegisteredRedirectUris(CLIENT_REDIRECT_URI);
    given(clientRepository.findOneByClientId(CLIENT_ID)).willReturn(Optional.of(client));

    creator.run();

    verify(clientRepository, times(1)).findOneByClientId(CLIENT_ID);
    verify(clientRepository, times(0)).saveAndFlush(any(Client.class));
  }

  @Test
  public void shouldUpdateSupersetClientAndSave() {
    Client client = new ClientDataBuilder().buildSupersetClient();
    given(clientRepository.findOneByClientId(CLIENT_ID)).willReturn(Optional.of(client));

    creator.run();

    verify(clientRepository).findOneByClientId(CLIENT_ID);
    verify(clientRepository).saveAndFlush(any(Client.class));

    assertThat(client.getClientSecret()).isEqualTo(CLIENT_SECRET);
  }

  @Test
  public void shouldCreateNewSupersetClient() {
    given(clientRepository.findOneByClientId(CLIENT_ID)).willReturn(Optional.empty());

    creator.run();

    verify(clientRepository, times(1)).findOneByClientId(CLIENT_ID);
    verify(clientRepository, times(1)).saveAndFlush(any(Client.class));
  }
}
