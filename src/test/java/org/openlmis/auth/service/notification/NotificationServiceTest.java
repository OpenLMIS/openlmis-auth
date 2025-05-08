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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.auth.service.notification.NotificationChannelDto.EMAIL;

import java.net.URI;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openlmis.auth.UserDataBuilder;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.exception.ExternalApiException;
import org.openlmis.auth.service.BaseCommunicationService;
import org.openlmis.auth.service.BaseCommunicationServiceTest;
import org.openlmis.util.NotificationRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;

public class NotificationServiceTest extends BaseCommunicationServiceTest {

  private static final String SUBJECT = "subject";
  private static final String PLAIN_CONTENT = "plainContent";

  @Captor
  private ArgumentCaptor<HttpEntity<NotificationRequest>> captor;

  private User user = new UserDataBuilder().build();

  @Test
  public void shouldSendNotification() {
    NotificationService service = prepareService();
    service.notify(user, SUBJECT, PLAIN_CONTENT);

    verify(restTemplate)
            .postForEntity(uriCaptor.capture(), captor.capture(), eq(NotificationDto.class));

    URI uri = uriCaptor.getValue();
    String url = service.getServiceUrl() + service.getUrl();
    assertThat(uri.toString(), is(equalTo(url)));

    HttpEntity entity = captor.getValue();
    Object body = entity.getBody();

    assertThat(body, instanceOf(NotificationDto.class));

    NotificationDto sent = (NotificationDto) body;

    assertThat(sent.getUserId(), is(user.getId()));
    assertThat(sent.getMessages().keySet().size(), is(1));

    assertThat(sent.getMessages().get(EMAIL.toString()).getSubject(), is(SUBJECT));
    assertThat(sent.getMessages().get(EMAIL.toString()).getBody(), is(PLAIN_CONTENT));

    assertAuthHeader(entity);
  }

  @Test(expected = HttpServerErrorException.class)
  public void shouldThrowExceptionIfCannotSendNotification() {
    when(restTemplate
            .postForEntity(any(URI.class), any(HttpEntity.class), eq(NotificationDto.class)))
            .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));

    NotificationService service = prepareService();
    service.notify(user, SUBJECT, PLAIN_CONTENT);
  }

  @Test(expected = ExternalApiException.class)
  public void shouldThrowExternalApiExceptionIfNotificationServiceReturnsBadRequestError() {
    when(restTemplate
        .postForEntity(any(URI.class), any(HttpEntity.class), eq(NotificationDto.class)))
        .thenThrow(new HttpServerErrorException(HttpStatus.BAD_REQUEST));

    NotificationService service = prepareService();
    service.notify(user, SUBJECT, PLAIN_CONTENT);
  }

  @Override
  protected BaseCommunicationService getService() {
    return new NotificationService();
  }

  @Override
  protected NotificationService prepareService() {
    BaseCommunicationService service = super.prepareService();

    ReflectionTestUtils.setField(service, "serviceUrl", "http://localhost/notification");

    return (NotificationService) service;
  }
}
