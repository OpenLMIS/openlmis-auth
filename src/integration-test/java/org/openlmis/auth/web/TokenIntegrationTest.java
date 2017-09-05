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

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TokenIntegrationTest extends BaseWebIntegrationTest {

  private static final int DURATION = 1000;

  @BeforeClass
  public static void setUpClass() {
    System.setProperty("TOKEN_DURATION", String.valueOf(DURATION));
  }

  @Test
  public void shouldSetExpirationForTokens() {
    DefaultOAuth2AccessToken token = restAssured.given()
        .auth().preemptive().basic("user-client", "changeme")
        .queryParam("grant_type", "password")
        .queryParam("username", "admin")
        .queryParam("password", "password")
        .when()
        .post("/api/oauth/token")
        .then()
        .statusCode(200)
        .extract().as(DefaultOAuth2AccessToken.class);

    assertNotNull(token);
    assertEquals(OAuth2AccessToken.BEARER_TYPE.toLowerCase(), token.getTokenType());
    // use a delta of five seconds - should be more than enough
    assertEquals(DURATION, token.getExpiresIn(), 5.0);
  }
}
