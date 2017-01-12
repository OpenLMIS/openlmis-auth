package org.openlmis.auth.service.notification;

import org.openlmis.auth.service.BaseCommunicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService extends BaseCommunicationService {

  @Value("${service.url}")
  private String notificationUrl;

  /**
    * Send a notification request.
    *
    * @param request details about notification.
    */
  public void send(NotificationRequest request) {
    String url = getNotificationUrl() + "/api/notification";

    Map<String, String> params = new HashMap<>();
    params.put(ACCESS_TOKEN, obtainAccessToken());

    HttpEntity<NotificationRequest> body = new HttpEntity<>(request);

    restTemplate.postForEntity(buildUri(url, params), body, NotificationRequest.class);
  }

  String getNotificationUrl() {
    return notificationUrl;
  }
}
