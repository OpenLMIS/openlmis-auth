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

package org.openlmis.auth.security;

import java.util.Date;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;

public class CustomTokenServices extends DefaultTokenServices {

  @Value("#{${token.validitySeconds} * 1000}")
  private Integer validityMs;

  @Override
  public OAuth2AccessToken readAccessToken(String accessToken) {
    DefaultOAuth2AccessToken token = (DefaultOAuth2AccessToken) super.readAccessToken(accessToken);

    Optional
        .ofNullable(token)
        .filter(this::hasExpirationDate)
        .filter(this::isNotExpired)
        .ifPresent(this::adjustExpirationDate);

    return token;
  }

  private boolean hasExpirationDate(OAuth2AccessToken token) {
    return null != token.getExpiration();
  }

  private boolean isNotExpired(OAuth2AccessToken token) {
    return !token.isExpired();
  }

  private void adjustExpirationDate(DefaultOAuth2AccessToken token) {
    token.setExpiration(new Date(System.currentTimeMillis() + validityMs));
  }

}
