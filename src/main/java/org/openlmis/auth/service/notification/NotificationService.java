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

package org.openlmis.auth.service.notification;

import static org.openlmis.auth.service.notification.NotificationChannelDto.EMAIL;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.service.BaseCommunicationService;
import org.openlmis.auth.util.RequestHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationService extends BaseCommunicationService<NotificationDto> {

  @Getter
  @Value("${service.url}")
  private String serviceUrl;

  protected String getUrl() {
    return "/api/notifications";
  }

  protected Class<NotificationDto> getResultClass() {
    return NotificationDto.class;
  }

  protected Class<NotificationDto[]> getArrayResultClass() {
    return NotificationDto[].class;
  }

  /**
   * Send an email notification.
   *
   * @param user    receiver of the notification
   * @param subject subject of the email
   * @param content content of the email
   */
  public void notify(User user, String subject, String content) {
    String url = getServiceUrl() + getUrl();

    NotificationDto request = buildNotification(user, subject, content);

    restTemplate.postForEntity(RequestHelper.createUri(url),
            RequestHelper.createEntity(obtainAccessToken(), request),
            NotificationDto.class);
  }

  private NotificationDto buildNotification(User user, String subject, String content) {
    Map<String, MessageDto> messages = new HashMap<>();
    messages.put(EMAIL.toString(), new MessageDto(subject, content));

    return new NotificationDto(user.getId(), messages, true);
  }
}
