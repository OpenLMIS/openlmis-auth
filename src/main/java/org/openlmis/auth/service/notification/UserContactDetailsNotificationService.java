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
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.auth.service.notification;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import lombok.Getter;
import org.openlmis.auth.service.BaseCommunicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserContactDetailsNotificationService extends
    BaseCommunicationService<UserContactDetailsDto> {

  @Getter
  @Value("${service.url}")
  private String serviceUrl;

  @Override
  protected String getUrl() {
    return "/api/userContactDetails";
  }

  @Override
  protected Class<UserContactDetailsDto> getResultClass() {
    return UserContactDetailsDto.class;
  }

  @Override
  protected Class<UserContactDetailsDto[]> getArrayResultClass() {
    return UserContactDetailsDto[].class;
  }

  /**
   * Finds user contact details by the passed email address.
   */
  public List<UserContactDetailsDto> findByEmail(String email) {
    ImmutableMap<String, Object> parameters = ImmutableMap
        .of("email", email, "page", 0, "size", Integer.MAX_VALUE);
    return getPage("", parameters).getContent();
  }

}
