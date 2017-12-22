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

import static org.openlmis.auth.i18n.MessageKeys.ERROR_TOKEN_REQUIRED;
import static org.springframework.security.oauth2.common.util.OAuth2Utils.GRANT_TYPE;

import com.google.common.collect.ImmutableMap;

import org.openlmis.auth.exception.ValidationMessageException;
import org.openlmis.auth.util.UuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.client.ClientCredentialsTokenGranter;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

@Service
public class AccessTokenService {

  @Autowired
  @Qualifier("clientDetailsServiceImpl")
  private ClientDetailsService clientDetailsService;

  @Autowired
  @Qualifier("defaultTokenServices")
  private DefaultTokenServices defaultTokenServices;

  private TokenGranter tokenGranter;
  private OAuth2RequestFactory requestFactory;

  /**
   * Initiates internal fields.
   */
  @PostConstruct
  public void init() {
    requestFactory = new DefaultOAuth2RequestFactory(clientDetailsService);
    tokenGranter = new ClientCredentialsTokenGranter(
        defaultTokenServices, clientDetailsService, requestFactory
    );
  }

  /**
   * Obtains token based on client ID and secret.
   */
  public UUID obtainToken(String clientId) {
    Map<String, String> parameters = ImmutableMap.of(GRANT_TYPE, "client_credentials");
    ClientDetails authenticatedClient = clientDetailsService.loadClientByClientId(clientId);

    TokenRequest tokenRequest = requestFactory
        .createTokenRequest(parameters, authenticatedClient);

    String token = tokenGranter
        .grant(tokenRequest.getGrantType(), tokenRequest)
        .getValue();

    return UuidUtil
        .fromString(token)
        .orElseThrow(() -> new ValidationMessageException(ERROR_TOKEN_REQUIRED));
  }
}
