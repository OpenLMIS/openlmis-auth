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

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * We verify that token store uses database.
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@Transactional
@SuppressWarnings("PMD.TooManyMethods")
public class CustomTokenStoreIntegrationTest {
  private static final String SELECT_ACCESS_TOKENS = "SELECT * FROM auth.oauth_access_token";
  private static final String SELECT_REFRESH_TOKENS = "SELECT * FROM auth.oauth_refresh_token";

  private static final String ACCESS_TOKEN_VALUE = UUID.randomUUID().toString();
  private static final String REFRESH_TOKEN_VALUE = UUID.randomUUID().toString();

  private static final String CLIENT_ID = "user-client";
  private static final String USER_NAME = "admin";

  @Autowired
  private CustomTokenStore tokenStore;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private DefaultOAuth2AccessToken token;
  private DefaultOAuth2RefreshToken refreshToken;

  private OAuth2Authentication authentication;

  @Before
  public void setUp() {
    refreshToken = new DefaultOAuth2RefreshToken(REFRESH_TOKEN_VALUE);

    token = new DefaultOAuth2AccessToken(ACCESS_TOKEN_VALUE);
    token.setRefreshToken(refreshToken);

    authentication = new OAuth2Authentication(
        new OAuth2Request(null, CLIENT_ID, null, true, null, null, null, null, null),
        new UsernamePasswordAuthenticationToken(USER_NAME, "password")
    );
  }

  @Test
  public void shouldInsertAccessToken() throws Exception {
    // checks in the method
    storeAccessToken();
  }

  @Test
  public void shouldSelectAccessToken() throws Exception {
    storeAccessToken();

    OAuth2AccessToken found = tokenStore.readAccessToken(token.getValue());
    assertThat(found, is(token));
  }

  @Test
  public void shouldSelectAccessTokenAuthentication() throws Exception {
    storeAccessToken();

    OAuth2Authentication found = tokenStore.readAuthentication(token);
    assertThat(found, is(authentication));
  }

  @Test
  public void shouldSelectAccessTokenFromAuthentication() throws Exception {
    storeAccessToken();

    OAuth2AccessToken found = tokenStore.getAccessToken(authentication);
    assertThat(found, is(token));
  }

  @Test
  public void shouldSelectAccessTokensFromUserNameAndClientId() throws Exception {
    storeAccessToken();

    Collection<OAuth2AccessToken> found = tokenStore
        .findTokensByClientIdAndUserName(CLIENT_ID, USER_NAME);

    assertThat(found, hasSize(1));
    assertThat(found.iterator().next(), is(token));
  }

  @Test
  public void shouldSelectAccessTokensFromUserName() throws Exception {
    storeAccessToken();
    Collection<OAuth2AccessToken> found = tokenStore.findTokensByUserName(USER_NAME);

    assertThat(found, hasSize(1));
    assertThat(found.iterator().next(), is(token));
  }

  @Test
  public void shouldSelectAccessTokensFromClientId() throws Exception {
    storeAccessToken();
    Collection<OAuth2AccessToken> found = tokenStore.findTokensByClientId(CLIENT_ID);

    assertThat(found, hasSize(1));
    assertThat(found.iterator().next(), is(token));
  }

  @Test
  public void shouldDeleteAccessToken() throws Exception {
    storeAccessToken();
    tokenStore.removeAccessToken(token.getValue());

    List<Map<String, Object>> rows = jdbcTemplate.queryForList(SELECT_ACCESS_TOKENS);
    assertThat(rows, hasSize(0));
  }

  @Test
  public void shouldInsertRefreshToken() throws Exception {
    // checks in the method
    storeRefreshToken();
  }

  @Test
  public void shouldSelectRefreshToken() throws Exception {
    storeRefreshToken();
    OAuth2RefreshToken found = tokenStore.readRefreshToken(refreshToken.getValue());

    assertThat(found, is(refreshToken));
  }

  @Test
  public void shouldSelectRefreshTokenAuthentication() throws Exception {
    storeRefreshToken();
    OAuth2Authentication found = tokenStore
        .readAuthenticationForRefreshToken(refreshToken.getValue());

    assertThat(found, is(authentication));
  }

  @Test
  public void shouldDeleteRefreshToken() throws Exception {
    storeRefreshToken();
    tokenStore.removeRefreshToken(refreshToken);

    List<Map<String, Object>> rows = jdbcTemplate.queryForList(SELECT_REFRESH_TOKENS);
    assertThat(rows, hasSize(0));
  }

  @Test
  public void shouldDeleteAccessTokenFromRefreshToken() throws Exception {
    storeAccessToken();
    storeRefreshToken();
    tokenStore.removeAccessTokenUsingRefreshToken(refreshToken);

    List<Map<String, Object>> rows = jdbcTemplate.queryForList(SELECT_ACCESS_TOKENS);
    assertThat(rows, hasSize(0));
  }

  private void storeAccessToken() throws Exception {
    tokenStore.storeAccessToken(token, authentication);

    List<Map<String, Object>> rows = jdbcTemplate.queryForList(SELECT_ACCESS_TOKENS);

    assertThat(rows, hasSize(1));

    Map<String, Object> row = rows.get(0);
    assertThat(row, hasEntry("tokenid", createTokenKey(ACCESS_TOKEN_VALUE)));
    assertThat(row, hasEntry("token", SerializationUtils.serialize(token)));
    assertThat(row, hasEntry("authenticationid", createAuthenticationKey()));
    assertThat(row, hasEntry("username", USER_NAME));
    assertThat(row, hasEntry("clientid", CLIENT_ID));
    assertThat(row, hasEntry("authentication", SerializationUtils.serialize(authentication)));
    assertThat(row, hasEntry("refreshtoken", createTokenKey(REFRESH_TOKEN_VALUE)));
  }

  private void storeRefreshToken() throws Exception {
    tokenStore.storeRefreshToken(refreshToken, authentication);

    List<Map<String, Object>> rows = jdbcTemplate.queryForList(SELECT_REFRESH_TOKENS);

    assertThat(rows, hasSize(1));

    Map<String, Object> row = rows.get(0);
    assertThat(row, hasEntry("tokenid", createTokenKey(REFRESH_TOKEN_VALUE)));
    assertThat(row, hasEntry("token", SerializationUtils.serialize(refreshToken)));
    assertThat(row, hasEntry("authentication", SerializationUtils.serialize(authentication)));
  }

  private String createTokenKey(String token) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("MD5");
    byte[] bytes = digest.digest(token.getBytes("UTF-8"));
    return String.format("%032x", new BigInteger(1, bytes));
  }

  private String createAuthenticationKey() {
    return new DefaultAuthenticationKeyGenerator().extractKey(authentication);
  }

}
