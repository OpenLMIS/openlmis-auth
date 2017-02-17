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

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "oauth_client_details")
public class Client {

  @Id
  @Getter
  @Setter
  @Column(name = "client_id")
  private String clientId;

  @Column(name = "client_secret")
  @Getter
  @Setter
  private String clientSecret;

  @Column(name = "scope")
  @Getter
  @Setter
  private String scope;

  @Column(name = "resource_ids")
  @Getter
  @Setter
  private String resourceIds;

  @Column(name = "authorized_grant_types")
  @Getter
  @Setter
  private String authorizedGrantTypes;

  @Column(name = "redirect_uri")
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

  @Column(name = "access_token_validity")
  @Getter
  @Setter
  private Integer accessTokenValiditySeconds;

  @Column(name = "refresh_token_validity")
  @Getter
  @Setter
  private Integer refreshTokenValiditySeconds;

  @Column(name = "additional_information")
  @Getter
  @Setter
  private String additionalInformation;

  @Column(name = "web_server_redirect_uri")
  @Getter
  @Setter
  private String webServerRedirectUri;
}
