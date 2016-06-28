package org.openlmis.auth.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClientDetails implements org.springframework.security.oauth2.provider.ClientDetails {

  @Getter
  @Setter
  private String clientId;

  @Getter
  @Setter
  private String clientSecret;

  @Getter
  @Setter
  private Set<String> scope = Collections.emptySet();

  @Getter
  @Setter
  private Set<String> resourceIds = Collections.emptySet();

  @Getter
  @Setter
  private Set<String> authorizedGrantTypes = Collections.emptySet();

  @Getter
  @Setter
  private Set<String> registeredRedirectUris;

  @Getter
  @Setter
  private Set<String> autoApproveScopes;

  @Getter
  @Setter
  private List<GrantedAuthority> authorities = Collections.emptyList();

  @Getter
  @Setter
  private Integer accessTokenValiditySeconds;

  @Getter
  @Setter
  private Integer refreshTokenValiditySeconds;

  @Getter
  @Setter
  private Map<String, Object> additionalInformation = new LinkedHashMap<String, Object>();

  @Override
  public boolean isSecretRequired() {
    return clientSecret != null && !clientSecret.isEmpty();
  }

  @Override
  public boolean isScoped() {
    return false;
  }

  @Override
  public Set<String> getRegisteredRedirectUri() {
    return registeredRedirectUris;
  }

  @Override
  public boolean isAutoApprove(String scope) {
    return false;
  }
}
