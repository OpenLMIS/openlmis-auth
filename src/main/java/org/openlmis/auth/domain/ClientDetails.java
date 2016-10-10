package org.openlmis.auth.domain;

import com.google.common.collect.ImmutableSet;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClientDetails implements org.springframework.security.oauth2.provider.ClientDetails {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientDetails.class);

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

  public ClientDetails(Client client) {
    this.clientId = client.getClientId();

    this.resourceIds = toSet(client.getResourceIds());
    this.scope = toSet(client.getScope());
    this.authorizedGrantTypes = toSet(client.getAuthorizedGrantTypes());
    this.authorities = toAuthorities(client.getAuthorities());
    this.registeredRedirectUris = toSet(client.getRegisteredRedirectUris());

    this.clientSecret = client.getClientSecret();
    this.accessTokenValiditySeconds = client.getAccessTokenValiditySeconds();
    this.refreshTokenValiditySeconds = client.getRefreshTokenValiditySeconds();

    String json = client.getAdditionalInformation();
    if (null != json) {
      try {
        this.additionalInformation = new ObjectMapper().readValue(json, Map.class);
      } catch (Exception e) {
        LOGGER.warn("Could not decode JSON for additional information: " + this, e);
      }
    }

    this.autoApproveScopes = toSet(client.getAutoApproveScopes());

    if (null == authorizedGrantTypes || authorizedGrantTypes.isEmpty()) {
      this.authorizedGrantTypes = ImmutableSet.of("authorization_code", "refresh_token");
    }
  }

  private Set<String> toSet(String arg) {
    if (StringUtils.hasText(arg)) {
      Set<String> resources = StringUtils.commaDelimitedListToSet(arg);

      if (!resources.isEmpty()) {
        return resources;
      }
    }

    return null;
  }

  private List<GrantedAuthority> toAuthorities(String arg) {
    return StringUtils.hasText(arg)
        ? AuthorityUtils.commaSeparatedStringToAuthorityList(arg)
        : null;
  }

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
