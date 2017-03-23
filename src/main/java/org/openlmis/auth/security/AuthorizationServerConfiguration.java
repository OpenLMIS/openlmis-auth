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


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

  @Autowired
  @Qualifier("authenticationManagerBean")
  private AuthenticationManager authenticationManager;

  @Autowired
  private TokenStore tokenStore;

  @Autowired
  @Qualifier("clientDetailsServiceImpl")
  private ClientDetailsService clientDetailsService;

  @Value("${token.validitySeconds}")
  private Integer tokenValiditySeconds;

  /**
   * Default token services bean initializer.
   * @return custom token services
   */
  @Bean
  public DefaultTokenServices defaultTokenServices() {
    DefaultTokenServices tokenServices = new CustomTokenServices();
    tokenServices.setTokenStore(tokenStore);
    tokenServices.setSupportRefreshToken(true);
    tokenServices.setClientDetailsService(clientDetailsService);
    tokenServices.setTokenEnhancer(tokenEnhancer());
    tokenServices.setAccessTokenValiditySeconds(tokenValiditySeconds);
    return tokenServices;
  }

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    endpoints.setClientDetailsService(clientDetailsService);
    endpoints
        .tokenStore(tokenStore)
        .authenticationManager(authenticationManager)
        .tokenServices(defaultTokenServices())
        .pathMapping("/oauth/token", "/api/oauth/token")
        .pathMapping("/oauth/check_token", "/api/oauth/check_token");
  }

  @Override
  public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
    oauthServer.checkTokenAccess("permitAll()");
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients.withClientDetails(clientDetailsService);
  }

  @Bean
  public TokenEnhancer tokenEnhancer() {
    return new AccessTokenEnhancer();
  }
}
