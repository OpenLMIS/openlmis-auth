package org.openlmis.auth.security;

import org.openlmis.auth.domain.User;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.Map;

public class AccessTokenEnhancer implements TokenEnhancer {

  @Override
  public OAuth2AccessToken enhance(OAuth2AccessToken accessToken,
                                   OAuth2Authentication authentication) {
    if (!authentication.isClientOnly()) {
      User user = (User) authentication.getPrincipal();
      final Map<String, Object> additionalInfo = new HashMap<>();

      additionalInfo.put("referenceDataUserId", user.getReferenceDataUserId());

      ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
    }
    return accessToken;
  }

}
