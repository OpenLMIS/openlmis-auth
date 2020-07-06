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

package org.openlmis.auth.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "oauth_client_details")
@NoArgsConstructor
@AllArgsConstructor
public class Client {

  @Id
  @Getter
  @Setter
  @Column(name = "clientid")
  private String clientId;

  @Column(name = "clientsecret")
  @Getter
  @Setter
  private String clientSecret;

  @Column(name = "scope")
  @Getter
  @Setter
  private String scope;

  @Column(name = "resourceids")
  @Getter
  @Setter
  private String resourceIds;

  @Column(name = "authorizedgranttypes")
  @Getter
  @Setter
  private String authorizedGrantTypes;

  @Column(name = "redirecturi")
  @Getter
  @Setter
  private String registeredRedirectUris;

  @Column(name = "autoapprove")
  @Getter
  @Setter
  private String autoApproveScopes;

  @Column(name = "authorities")
  @Getter
  @Setter
  private String authorities;

  @Column(name = "accesstokenvalidity")
  @Getter
  @Setter
  private Integer accessTokenValiditySeconds;

  @Column(name = "refreshtokenvalidity")
  @Getter
  @Setter
  private Integer refreshTokenValiditySeconds;

  @Column(name = "additionalinformation")
  @Getter
  @Setter
  private String additionalInformation;

  @Column(name = "webserverredirecturi")
  @Getter
  @Setter
  private String webServerRedirectUri;

  /**
   * Creates new instance of {@link Client}.
   */
  public Client(String clientId, String clientSecret, String authorities,
                String authorizedGrantTypes, String scope, Integer accessTokenValiditySeconds) {
    this(
        clientId, clientSecret, scope, null, authorizedGrantTypes, null, null,authorities,
        accessTokenValiditySeconds,null, null, null
    );
  }

  /**
   * Creates new instance of {@link Client} with registeredRedirectUris.
   */
  public Client(String clientId, String clientSecret, String authorities,
                String registeredRedirectUris, String authorizedGrantTypes, String scope,
                Integer accessTokenValiditySeconds, String resourceIds) {
    this(
          clientId, clientSecret, scope, resourceIds, authorizedGrantTypes,
            registeredRedirectUris, null, authorities, accessTokenValiditySeconds,null,
            null, null
    );
  }
}
