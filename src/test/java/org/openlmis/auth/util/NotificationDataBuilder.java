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

package org.openlmis.auth.util;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
import org.openlmis.auth.service.notification.MessageDto;
import org.openlmis.auth.service.notification.NotificationDto;

public class NotificationDataBuilder {
  private UUID userId = UUID.randomUUID();
  private Map<String, MessageDto> messages = Maps.newHashMap();

  public NotificationDataBuilder withUserId(UUID userId) {
    this.userId = userId;
    return this;
  }

  public NotificationDataBuilder withMessage(String messageType, MessageDto message) {
    this.messages.put(messageType, message);
    return this;
  }

  public NotificationDataBuilder withMessage(String messageType, String subject, String body) {
    return withMessage(messageType, new MessageDto(subject, body, false));
  }

  public NotificationDataBuilder withEmptyMessage(String messageType) {
    this.messages.put(messageType, new MessageDto());
    return this;
  }

  public NotificationDto build() {
    return new NotificationDto(userId, messages);
  }
}
