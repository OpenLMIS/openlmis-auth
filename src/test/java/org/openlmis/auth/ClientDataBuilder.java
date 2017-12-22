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

import org.openlmis.auth.domain.Client;

public class ClientDataBuilder {
  private String clientId = "user-client";
  private String clientSecret = "changeme";
  private String scope = "read,write";
  private String resourceIds = "auth,referencedata";
  private String authorizedGrantTypes = "password";
  private String registeredRedirectUris = null;
  private String autoApproveScopes = null;
  private String authorities = "TRUSTED_CLIENT";
  private Integer accessTokenValiditySeconds = null;
  private Integer refreshTokenValiditySeconds = null;
  private String additionalInformation = null;
  private String webServerRedirectUri = null;

  /**
   * Creates a new instance of {@link Client} as service client.
   */
  public Client buildUserClient() {
    this.clientId = "user-client";
    this.authorizedGrantTypes = "password";
    this.accessTokenValiditySeconds = null;

    return build();
  }

  /**
   * Creates a new instance of {@link Client} as service client.
   */
  public Client buildServiceClient() {
    this.clientId = "trusted-client";
    this.authorizedGrantTypes = "client_credentials";
    this.accessTokenValiditySeconds = null;

    return build();
  }

  private Client build() {
    return new Client(
        clientId, clientSecret, scope, resourceIds, authorizedGrantTypes, registeredRedirectUris,
        autoApproveScopes, authorities, accessTokenValiditySeconds, refreshTokenValiditySeconds,
        additionalInformation, webServerRedirectUri
    );
  }
}
