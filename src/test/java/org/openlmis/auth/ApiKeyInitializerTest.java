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

package org.openlmis.auth;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.auth.domain.ApiKey;
import org.openlmis.auth.domain.ClientDetails;
import org.openlmis.auth.repository.ApiKeyRepository;
import org.openlmis.auth.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class ApiKeyInitializerTest {

  @Mock
  private TokenStore tokenStore;

  @Mock
  private ApiKeyRepository apiKeyRepository;

  @InjectMocks
  private ApiKeyInitializer initializer;

  @Captor
  private ArgumentCaptor<OAuth2AccessToken> tokenCaptor;

  @Captor
  private ArgumentCaptor<OAuth2Authentication> authenticationCaptor;

  private Pageable pageable = new PageRequest(Pagination.DEFAULT_PAGE_NUMBER, 2000);

  private ApiKey apiKey = new ApiKeyDataBuilder().build();

  private Page<ApiKey> emptyPage = new PageImpl<>(Lists.newArrayList());
  private Page<ApiKey> firstPage = new PageImpl<>(Lists.newArrayList(apiKey));

  @Test
  public void shouldDoNothingIfThereIsNoApiKeys() {
    when(apiKeyRepository.findAll(pageable)).thenReturn(emptyPage);

    initializer.run();

    verifyZeroInteractions(tokenStore);
  }

  @Test
  public void shouldAddApiKeyToAccessTokenStore() {
    when(apiKeyRepository.findAll(pageable)).thenReturn(firstPage);

    initializer.run();

    verify(tokenStore).storeAccessToken(tokenCaptor.capture(), authenticationCaptor.capture());

    OAuth2AccessToken token = tokenCaptor.getValue();
    assertThat(token, is(notNullValue()));
    assertThat(token.getValue(), is(apiKey.getToken().toString()));

    OAuth2Authentication authentication = authenticationCaptor.getValue();
    assertThat(authentication, is(notNullValue()));
    assertThat(authentication.getOAuth2Request(), is(notNullValue()));
    // api key is not related with any user
    assertThat(authentication.getUserAuthentication(), is(nullValue()));

    OAuth2Request request = authentication.getOAuth2Request();
    ClientDetails clientDetails = new ClientDetails(apiKey.getClient());

    assertThat(request.getRequestParameters(), is(Collections.emptyMap()));
    assertThat(request.getClientId(), is(clientDetails.getClientId()));
    assertThat(request.getAuthorities(), contains(clientDetails.getAuthorities().toArray()));
    assertThat(request.isApproved(), is(true));
    assertThat(request.getScope(), is(clientDetails.getScope()));
    assertThat(request.getResourceIds(), is(clientDetails.getResourceIds()));
    assertThat(request.getRedirectUri(), is(nullValue()));
    assertThat(request.getResponseTypes(), is(Collections.emptySet()));
    assertThat(request.getExtensions(), is(Collections.emptyMap()));
  }
}
