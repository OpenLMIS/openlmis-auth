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

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Date;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.test.util.ReflectionTestUtils;

public class CustomTokenServicesTest {
  private static final String ACCESS_TOKEN = UUID.randomUUID().toString();
  private static final Date CURRENT_DATE = new Date(System.currentTimeMillis());

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private TokenStore tokenStore;

  @Mock
  private DefaultOAuth2AccessToken token;

  @Captor
  private ArgumentCaptor<Date> dateCaptor;

  private CustomTokenServices tokenServices;

  @Before
  public void setUp() {
    tokenServices = new CustomTokenServices();
    tokenServices.setTokenStore(tokenStore);

    ReflectionTestUtils.setField(tokenServices, "validityMs", 1000);

    given(tokenStore.readAccessToken(ACCESS_TOKEN)).willReturn(token);
  }

  @Test
  public void shouldIncreaseExpirationForStandardTokens() {
    // given
    given(token.getExpiration()).willReturn(CURRENT_DATE);
    given(token.isExpired()).willReturn(false);

    // when
    OAuth2AccessToken result = tokenServices.readAccessToken(ACCESS_TOKEN);

    // then
    verify(token).setExpiration(dateCaptor.capture());
    assertThat(result).isEqualTo(token);

    Date expirationDate = dateCaptor.getValue();
    assertThat(expirationDate).isAfter(CURRENT_DATE);
  }

  @Test
  public void shouldNotIncreaseExpirationForExpiredTokens() {
    // given
    given(token.getExpiration()).willReturn(CURRENT_DATE);
    given(token.isExpired()).willReturn(true);

    // when
    OAuth2AccessToken result = tokenServices.readAccessToken(ACCESS_TOKEN);

    // then
    verify(token, never()).setExpiration(any(Date.class));
    assertThat(result).isEqualTo(token);
  }

  @Test
  public void shouldNotIncreaseExpirationForNonExpiredTokens() {
    // given
    given(token.getExpiration()).willReturn(null);

    // when
    OAuth2AccessToken result = tokenServices.readAccessToken(ACCESS_TOKEN);

    // then
    verify(token, never()).setExpiration(any(Date.class));
    assertThat(result).isEqualTo(token);
  }

  @Test
  public void shouldNotIncreaseExpirationIfTokenDoesNotExist() {
    // given
    given(tokenStore.readAccessToken(ACCESS_TOKEN)).willReturn(null);

    // when
    OAuth2AccessToken result = tokenServices.readAccessToken(ACCESS_TOKEN);

    // then
    verify(token, never()).setExpiration(any(Date.class));
    assertThat(result).isNull();
  }
}
