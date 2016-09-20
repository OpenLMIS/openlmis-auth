package org.openlmis.auth.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "clients")
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
  private String scope;

  @Column(name = "resource_ids")
  @Getter
  private String resourceIds;

  @Column(name = "authorized_grant_types")
  @Getter
  private String authorizedGrantTypes;

  @Column(name = "redirect_uri")
  @Getter
  private String registeredRedirectUris;

  @Column(name = "autoapprove")
  @Getter
  private String autoApproveScopes;

  @Column(name = "authorities")
  @Getter
  private String authorities;

  @Column(name = "access_token_validity")
  @Getter
  private Integer accessTokenValiditySeconds;

  @Column(name = "refresh_token_validity")
  @Getter
  private Integer refreshTokenValiditySeconds;

  @Column(name = "additional_information")
  @Getter
  private String additionalInformation;

  @Column(name = "web_server_redirect_uri")
  @Getter
  private String webServerRedirectUri;
}
