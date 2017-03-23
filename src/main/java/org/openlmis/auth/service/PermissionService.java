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

import org.openlmis.auth.dto.ResultDto;
import org.openlmis.auth.dto.RightDto;
import org.openlmis.auth.dto.referencedata.UserDto;
import org.openlmis.auth.service.referencedata.UserReferenceDataService;
import org.openlmis.auth.web.PermissionMessageException;
import org.openlmis.auth.util.AuthenticationHelper;
import org.openlmis.auth.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.openlmis.auth.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;

@SuppressWarnings("PMD.TooManyMethods")
@Service
public class PermissionService {
  static final String USERS_MANAGE = "USERS_MANAGE";

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  public void canManageUsers() {
    checkPermission(USERS_MANAGE, null, null, null);
  }

  private void checkPermission(String rightName, UUID program, UUID facility, UUID warehouse) {
    if (!hasPermission(rightName, program, facility, warehouse)) {
      throw new PermissionMessageException(new Message(ERROR_NO_FOLLOWING_PERMISSION, rightName));
    }
  }

  private Boolean hasPermission(String rightName, UUID program, UUID facility, UUID warehouse) {
    UserDto user = authenticationHelper.getCurrentUser();
    RightDto right = authenticationHelper.getRight(rightName);
    ResultDto<Boolean> result = userReferenceDataService.hasRight(
        user.getId(), right.getId(), program, facility, warehouse
    );
    return null != result && result.getResult();
  }


}
