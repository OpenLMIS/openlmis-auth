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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openlmis.auth.service.BaseCommunicationService;
import org.openlmis.auth.service.BaseCommunicationServiceTest;
import org.openlmis.util.NotificationRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;

import java.net.URI;

public class NotificationServiceTest extends BaseCommunicationServiceTest {

  @Captor
  private ArgumentCaptor<HttpEntity<NotificationRequest>> captor;

  @Test
  public void shouldSendNotification() {
    NotificationRequest request = new NotificationRequest("from", "to", "subject", "plainContent");

    NotificationService service = prepareService();
    service.send(request);

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
  }

  @Test(expected = HttpServerErrorException.class)
  public void shouldReturnFalseIfCannotSendNotification() {
    NotificationRequest request = new NotificationRequest("from", "to", "subject", "plainContent");

    when(restTemplate
            .postForEntity(any(URI.class), any(HttpEntity.class), eq(NotificationRequest.class)))
            .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));

    NotificationService service = prepareService();
    service.send(request);
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
