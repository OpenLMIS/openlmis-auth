package org.openlmis.auth.service.notification;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class NotificationRequest {
  private final String from;
  private final String to;
  private final String subject;
  private final String content;
  private final String htmlContent;

  public static NotificationRequest plainTextNotification(String from, String to, String subject,
                                                            String content) {
    return new NotificationRequest(from, to, subject, content, null);
  }
}
