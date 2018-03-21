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

import org.openlmis.auth.domain.ApiKey;
import org.openlmis.auth.domain.Client;
import org.openlmis.auth.domain.ClientDetails;
import org.openlmis.auth.repository.ApiKeyRepository;
import org.openlmis.auth.util.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;

/**
 * ApiKeyInitializer runs after its associated Spring application has loaded.
 * It recreated API keys to work after the system restart.
 */
@Component
@Order(20)
public class ApiKeyInitializer implements CommandLineRunner {

  @Autowired
  private TokenStore tokenStore;

  @Autowired
  private ApiKeyRepository apiKeyRepository;

  /**
   * This method is part of CommandLineRunner and is called automatically by Spring.
   * @param args Main method arguments.
   */
  public void run(String... args) {
    Pagination.handlePage(apiKeyRepository::findAll, this::fixApiKey);
  }

  private void fixApiKey(ApiKey apiKey) {
    String tokenValue = apiKey.getToken().toString();

    Client client = apiKey.getClient();
    ClientDetails clientDetails = new ClientDetails(client);

    OAuth2AccessToken token = new DefaultOAuth2AccessToken(tokenValue);
    OAuth2Request storedRequest = new OAuth2Request(
        null, clientDetails.getClientId(), clientDetails.getAuthorities(),
        true, clientDetails.getScope(), clientDetails.getResourceIds(),
        null, null, null
    );
    OAuth2Authentication authentication = new OAuth2Authentication(storedRequest, null);

    tokenStore.storeAccessToken(token, authentication);
  }

}
