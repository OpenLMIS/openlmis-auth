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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.openlmis.auth.ClientDataBuilder;
import org.openlmis.auth.domain.Client;
import org.openlmis.auth.domain.ClientDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;

@RunWith(MockitoJUnitRunner.class)
public class AccessTokenServiceTest {
  private static final UUID TOKEN = UUID.randomUUID();

  @Mock(name = "clientDetailsServiceImpl")
  private ClientDetailsService clientDetailsService;

  @Mock(name = "defaultTokenServices")
  private DefaultTokenServices defaultTokenServices;

  @InjectMocks
  private AccessTokenService accessTokenService;

  private Client client = new ClientDataBuilder().buildServiceClient();

  @Before
  public void setUp() {
    accessTokenService.init();
  }

  @Test
  public void shouldObtainToken() {
    // when
    when(clientDetailsService.loadClientByClientId(client.getClientId()))
        .thenReturn(new ClientDetails(client));
    when(defaultTokenServices.createAccessToken(any(OAuth2Authentication.class)))
        .thenAnswer(new CreateAccessTokenAnswer());

    UUID token = accessTokenService.obtainToken(client.getClientId());

    // then
    assertThat(token, is(equalTo(TOKEN)));

    verify(clientDetailsService, atLeastOnce()).loadClientByClientId(client.getClientId());
  }

  private final class CreateAccessTokenAnswer implements Answer<OAuth2AccessToken> {

    @Override
    public OAuth2AccessToken answer(InvocationOnMock invocation) {
      OAuth2Authentication arg = invocation.getArgument(0, OAuth2Authentication.class);
      assertThat(arg.getOAuth2Request().getClientId(), is(client.getClientId()));
      return new DefaultOAuth2AccessToken(TOKEN.toString());
    }

  }
}
