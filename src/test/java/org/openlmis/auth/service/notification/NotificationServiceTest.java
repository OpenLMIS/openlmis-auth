package org.openlmis.auth.service.notification;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openlmis.auth.service.BaseCommunicationService;
import org.openlmis.auth.service.BaseCommunicationServiceTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;

import java.net.URI;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NotificationServiceTest extends BaseCommunicationServiceTest {

  @Captor
  private ArgumentCaptor<HttpEntity<NotificationRequest>> captor;

  @Test
  public void shouldSendNotification() {
    NotificationRequest request = NotificationRequest
            .plainTextNotification("from", "to", "subject", "plainContent");

    NotificationService service = prepareService();
    boolean success = service.send(request);

    assertThat(success, is(true));

    verify(restTemplate)
            .postForEntity(uriCaptor.capture(), captor.capture(), eq(NotificationRequest.class));

    URI uri = uriCaptor.getValue();
    String url = service.getNotificationUrl() + "/api/notification?" + ACCESS_TOKEN;
    assertThat(uri.toString(), is(equalTo(url)));

    HttpEntity entity = captor.getValue();
    Object body = entity.getBody();

    assertThat(body, instanceOf(NotificationRequest.class));

    NotificationRequest sent = (NotificationRequest) body;

    assertThat(sent.getFrom(), is(equalTo(request.getFrom())));
    assertThat(sent.getTo(), is(equalTo(request.getTo())));
    assertThat(sent.getSubject(), is(equalTo(request.getSubject())));
    assertThat(sent.getContent(), is(equalTo(request.getContent())));
    assertThat(sent.getHtmlContent(), is(equalTo(request.getHtmlContent())));
  }

  @Test
  public void shouldReturnFalseIfCannotSendNotification() {
    NotificationRequest request = NotificationRequest
            .plainTextNotification("from", "to", "subject", "plainContent");

    when(restTemplate
            .postForEntity(any(URI.class), any(HttpEntity.class), eq(NotificationRequest.class)))
            .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));

    NotificationService service = prepareService();
    boolean success = service.send(request);

    assertThat(success, is(false));
  }

  @Override
  protected BaseCommunicationService getService() {
    return new NotificationService();
  }

  @Override
  protected NotificationService prepareService() {
    BaseCommunicationService service = super.prepareService();

    ReflectionTestUtils.setField(service, "notificationUrl", "http://localhost/notification");

    return (NotificationService) service;
  }
}
