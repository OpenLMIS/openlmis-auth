package org.openlmis.auth.service.notification;

import org.openlmis.auth.service.BaseCommunicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService extends BaseCommunicationService {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Value("${service.url}")
  private String notificationUrl;

  /**
    * Send a notification request.
    *
    * @param request details about notification.
    * @return true if success, false if failed.
    */
  public boolean send(NotificationRequest request) {
    String url = getNotificationUrl() + "/api/notification";

    Map<String, String> params = new HashMap<>();
    params.put(ACCESS_TOKEN, obtainAccessToken());

    HttpEntity<NotificationRequest> body = new HttpEntity<>(request);

    try {
      restTemplate.postForEntity(buildUri(url, params), body, NotificationRequest.class);
    } catch (RestClientException ex) {
      logger.error("Can not send a notification request", ex);
      return false;
    }

    return true;
  }

  String getNotificationUrl() {
    return notificationUrl;
  }
}
