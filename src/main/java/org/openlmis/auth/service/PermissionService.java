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

import static org.openlmis.auth.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;

import org.openlmis.auth.dto.ResultDto;
import org.openlmis.auth.dto.RightDto;
import org.openlmis.auth.dto.referencedata.UserDto;
import org.openlmis.auth.exception.PermissionMessageException;
import org.openlmis.auth.service.referencedata.UserReferenceDataService;
import org.openlmis.auth.util.AuthenticationHelper;
import org.openlmis.auth.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PermissionService {
  public static final String USERS_MANAGE = "USERS_MANAGE";
  public static final String SERVICE_ACCOUNTS_MANAGE = "SERVICE_ACCOUNTS_MANAGE";

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private ApiKeySettings apiKeySettings;

  @Value("${auth.server.clientId}")
  private String serviceTokenClientId;

  /**
   * Checks whether user can edit password of the user with the given username.
   *
   * @param username  the username of the user
   */
  public void canEditUserPassword(String username) {
    if (username.equals(authenticationHelper.getCurrentUser().getUsername())) {
      return;
    }
    canManageUsers(null);
  }

  /**
   * Checks whether user can resend verification email of the user with the given id.
   *
   */
  public void canVerifyEmail(UUID referenceDataUserId) {
    if (referenceDataUserId.equals(authenticationHelper.getCurrentUser().getId())) {
      return;
    }

    canManageUsers(null);
  }

  public void canManageUsers(UUID referenceDataId) {
    checkPermission(USERS_MANAGE, false, referenceDataId);
  }

  public void canManageApiKeys() {
    checkPermission(SERVICE_ACCOUNTS_MANAGE, false, null);
  }

  private void checkPermission(String rightName, boolean allowApiKey, UUID expectedUserId) {
    if (!hasRight(rightName, allowApiKey, expectedUserId)) {
      // at this point, token is unauthorized
      throw new PermissionMessageException(new Message(ERROR_NO_FOLLOWING_PERMISSION, rightName));
    }
  }

  public boolean hasRight(String rightName) {
    return hasRight(rightName, false, null);
  }

  private boolean hasRight(String rightName, boolean allowApiKey, UUID expectedUserId) {
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder
        .getContext()
        .getAuthentication();

    if (authentication.isClientOnly()) {
      return checkServiceToken(allowApiKey, authentication);
    } else {
      return checkUserToken(rightName, expectedUserId);
    }
  }

  private boolean checkUserToken(String rightName, UUID expectedUserId) {
    UserDto user = authenticationHelper.getCurrentUser();

    if (user.getId().equals(expectedUserId)) {
      return true;
    }

    RightDto right = authenticationHelper.getRight(rightName);
    ResultDto<Boolean> result = userReferenceDataService.hasRight(
        user.getId(), right.getId()
    );

    return null != result && result.getResult();
  }

  private boolean checkServiceToken(boolean allowApiKey, OAuth2Authentication authentication) {
    String clientId = authentication.getOAuth2Request().getClientId();

    if (serviceTokenClientId.equals(clientId)) {
      return true;
    } else if (apiKeySettings.isApiKey(clientId)) {
      return allowApiKey;
    }

    return false;
  }


}
