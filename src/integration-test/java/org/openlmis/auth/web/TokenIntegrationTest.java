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

package org.openlmis.auth.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.openlmis.auth.web.TestWebData.Fields;
import static org.openlmis.auth.web.TestWebData.GrantTypes;
import static org.openlmis.auth.web.TestWebData.Tokens.DURATION;

import java.util.Optional;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openlmis.auth.ApiKeyDataBuilder;
import org.openlmis.auth.DummyUserMainDetailsDto;
import org.openlmis.auth.domain.ApiKey;
import org.openlmis.auth.domain.Client;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

public class TokenIntegrationTest extends BaseWebIntegrationTest {

  @BeforeClass
  public static void setUpClass() {
    System.setProperty("TOKEN_DURATION", String.valueOf(DURATION));
  }

  @Test
  public void shouldSetExpirationForTokens() {
    Client client = mockUserClient();
    OAuth2AccessToken token = startRequest()
        .auth()
        .preemptive()
        .basic(client.getClientId(), client.getClientSecret())
        .queryParam(Fields.PASSWORD, GrantTypes.PASSWORD)
        .queryParam(Fields.USERNAME, DummyUserMainDetailsDto.USERNAME)
        .queryParam(Fields.GRANT_TYPE, DummyUserMainDetailsDto.PASSWORD)
        .when()
        .post("/api/oauth/token")
        .then()
        .statusCode(200)
        .extract()
        .as(OAuth2AccessToken.class);

    assertNotNull(token);
    assertEquals(OAuth2AccessToken.BEARER_TYPE.toLowerCase(), token.getTokenType());
    // use a delta of five seconds - should be more than enough
    assertEquals(DURATION, token.getExpiresIn(), 5.0);
  }

  @Test
  public void shouldNotSetExpirationForApiKeys() {
    // given
    final ApiKey key = new ApiKeyDataBuilder().build();
    final Client keyClient = key.getClient();

    given(clientRepository.findOneByClientId(keyClient.getClientId()))
        .willReturn(Optional.of(keyClient));

    OAuth2AccessToken token = startRequest()
        .auth()
        .preemptive()
        .basic(keyClient.getClientId(), keyClient.getClientSecret())
        .queryParam(Fields.GRANT_TYPE, "client_credentials")
        .when()
        .post("/api/oauth/token")
        .then()
        .statusCode(200)
        .extract()
        .as(OAuth2AccessToken.class);

    assertNotNull(token);
    assertEquals(OAuth2AccessToken.BEARER_TYPE.toLowerCase(), token.getTokenType());
    assertNull(token.getExpiration());
  }
}
