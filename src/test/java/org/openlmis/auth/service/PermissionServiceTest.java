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

package org.openlmis.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.openlmis.auth.OAuth2AuthenticationDataBuilder.API_KEY_PREFIX;
import static org.openlmis.auth.OAuth2AuthenticationDataBuilder.SERVICE_CLIENT_ID;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;
import static org.openlmis.auth.service.PermissionService.SERVICE_ACCOUNTS_MANAGE;
import static org.openlmis.auth.service.PermissionService.USERS_MANAGE;

import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.auth.DummyRightDto;
import org.openlmis.auth.DummyUserMainDetailsDto;
import org.openlmis.auth.OAuth2AuthenticationDataBuilder;
import org.openlmis.auth.dto.ResultDto;
import org.openlmis.auth.dto.RightDto;
import org.openlmis.auth.dto.referencedata.UserMainDetailsDto;
import org.openlmis.auth.exception.PermissionMessageException;
import org.openlmis.auth.service.referencedata.UserReferenceDataService;
import org.openlmis.auth.util.AuthenticationHelper;
import org.openlmis.auth.util.Message;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class PermissionServiceTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Mock
  private AuthenticationHelper authenticationHelper;

  @Mock
  private UserReferenceDataService userReferenceDataService;

  @Spy
  private ApiKeySettings apiKeySettings;

  @InjectMocks
  private PermissionService permissionService;

  private SecurityContext securityContext;

  private OAuth2Authentication trustedClient;
  private OAuth2Authentication userClient;
  private OAuth2Authentication apiKeyClient;

  private UserMainDetailsDto user = new DummyUserMainDetailsDto();
  private RightDto right = new DummyRightDto();

  @Before
  public void setUp() {
    securityContext = mock(SecurityContext.class);
    SecurityContextHolder.setContext(securityContext);

    trustedClient = new OAuth2AuthenticationDataBuilder().buildServiceAuthentication();
    userClient = new OAuth2AuthenticationDataBuilder()
        .withReferenceDataUserId(UUID.fromString(DummyUserMainDetailsDto.REFERENCE_ID))
        .buildUserAuthentication();
    apiKeyClient = new OAuth2AuthenticationDataBuilder().buildApiKeyAuthentication();

    when(authenticationHelper.getCurrentUser()).thenReturn(user);
    when(authenticationHelper.getRight(USERS_MANAGE)).thenReturn(right);
    when(authenticationHelper.getRight(SERVICE_ACCOUNTS_MANAGE)).thenReturn(right);
    when(userReferenceDataService.hasRight(user.getId(), right.getId()))
        .thenReturn(new ResultDto<>(true));

    ReflectionTestUtils.setField(permissionService, "serviceTokenClientId", SERVICE_CLIENT_ID);
    ReflectionTestUtils.setField(apiKeySettings, "prefix", API_KEY_PREFIX);
  }

  @Test
  public void userShouldBeAbleToManageUsers() {
    when(securityContext.getAuthentication()).thenReturn(userClient);
    permissionService.canManageUsers(null);
  }

  @Test
  public void userShouldBeAbleToManageOwnData() {
    when(securityContext.getAuthentication()).thenReturn(userClient);
    permissionService.canManageUsers(UUID.fromString(DummyUserMainDetailsDto.REFERENCE_ID));
  }

  @Test(expected = PermissionMessageException.class)
  public void userShouldNotBeAbleToManageUsersIfHasNoRight() {
    when(securityContext.getAuthentication()).thenReturn(userClient);
    when(userReferenceDataService.hasRight(user.getId(), right.getId()))
        .thenReturn(new ResultDto<>(false));

    permissionService.canManageUsers(null);
  }

  @Test
  public void serviceShouldBeAbleToManageUsers() {
    when(securityContext.getAuthentication()).thenReturn(trustedClient);
    permissionService.canManageUsers(null);
  }

  @Test(expected = PermissionMessageException.class)
  public void apiKeyShouldNotBeAbleToManageUsers() {
    when(securityContext.getAuthentication()).thenReturn(apiKeyClient);
    permissionService.canManageUsers(null);
  }

  @Test
  public void userShouldBeAbleToManageApiKeys() {
    when(securityContext.getAuthentication()).thenReturn(userClient);
    permissionService.canManageApiKeys();
  }

  @Test
  public void serviceShouldBeAbleToManageApiKeys() {
    when(securityContext.getAuthentication()).thenReturn(trustedClient);
    permissionService.canManageApiKeys();
  }

  @Test(expected = PermissionMessageException.class)
  public void apiKeyShouldNotBeAbleToManageApiKeys() {
    when(securityContext.getAuthentication()).thenReturn(apiKeyClient);
    permissionService.canManageApiKeys();
  }

  @Test(expected = PermissionMessageException.class)
  public void serviceShouldNotBeAbleToManageApiKeysWhenIdsDoesNotMatch() {
    when(securityContext.getAuthentication()).thenReturn(apiKeyClient);
    permissionService.canManageApiKeys();
  }

  @Test
  public void userShouldBeAbleToEditOwnPassword() {
    permissionService.canEditUserPassword(user.getUsername());

    verify(authenticationHelper).getCurrentUser();
    verify(authenticationHelper, never()).getRight(anyString());
    verifyZeroInteractions(userReferenceDataService, apiKeySettings);
  }

  @Test
  public void userWithoutRightShouldBeUnableToEditOtherUsersPassword() {
    when(securityContext.getAuthentication()).thenReturn(userClient);
    when(userReferenceDataService.hasRight(user.getId(), right.getId()))
        .thenReturn(new ResultDto<>(false));

    exception.expect(PermissionMessageException.class);
    exception.expectMessage(new Message(ERROR_NO_FOLLOWING_PERMISSION, right.getName()).toString());

    permissionService.canEditUserPassword("OtherUser");
  }

  @Test
  public void userWithRightShouldBeUnableToEditOtherUsersPassword() {
    when(securityContext.getAuthentication()).thenReturn(userClient);
    when(userReferenceDataService.hasRight(user.getId(), right.getId()))
        .thenReturn(new ResultDto<>(true));

    permissionService.canEditUserPassword("OtherUser");
  }

  @Test
  public void shouldReturnTrueIfUserHasRight() {
    when(securityContext.getAuthentication()).thenReturn(userClient);
    when(userReferenceDataService.hasRight(user.getId(), right.getId()))
        .thenReturn(new ResultDto<>(true));

    assertThat(permissionService.hasRight(USERS_MANAGE)).isTrue();
  }

  @Test
  public void shouldReturnFalseIfUserHasNoRight() {
    when(securityContext.getAuthentication()).thenReturn(userClient);
    when(userReferenceDataService.hasRight(user.getId(), right.getId()))
        .thenReturn(new ResultDto<>(false));

    assertThat(permissionService.hasRight(USERS_MANAGE)).isFalse();
  }

}
