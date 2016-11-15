package org.openlmis.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;

import java.util.Date;

public class CustomTokenServices extends DefaultTokenServices {

  @Value("${token.validitySeconds}")
  private Integer validitySeconds;

  @Override
  public OAuth2AccessToken readAccessToken(String accessToken) {
    DefaultOAuth2AccessToken token = (DefaultOAuth2AccessToken)
        super.readAccessToken(accessToken);
    if (null != token && !token.isExpired()) {
      token.setExpiration(new Date(System.currentTimeMillis() + (validitySeconds * 1000L)));
    }
    return token;
  }

}
