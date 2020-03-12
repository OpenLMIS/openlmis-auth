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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.auth.DummyUserMainDetailsDto;
import org.openlmis.auth.domain.ExpirationToken;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.i18n.ExposedMessageSource;
import org.openlmis.auth.repository.ExpirationTokenRepository;
import org.openlmis.auth.service.notification.NotificationService;
import org.openlmis.auth.service.referencedata.UserReferenceDataService;

@RunWith(MockitoJUnitRunner.class)
public abstract class ExpirationTokenNotifierTest<T extends ExpirationToken> {

  @Mock
  private UserReferenceDataService userReferenceDataService;

  @Mock
  private NotificationService notificationService;

  @Mock
  private ExposedMessageSource messageSource;

  @Mock
  User user;

  @Before
  public void setUp() {
    when(userReferenceDataService.findOne(any(UUID.class)))
        .thenReturn(new DummyUserMainDetailsDto());
    when(messageSource.getMessage(anyString(), any(String[].class), any(Locale.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, String.class));
  }

  abstract ExpirationTokenNotifier<T> getNotifier();

  abstract ExpirationTokenRepository<T> getRepository();

  abstract T getToken();

  @Test
  public void shouldRemoveOldTokenIfExist() {
    // given
    ExpirationTokenRepository<T> repository = getRepository();
    T token = getToken();

    // when
    when(repository.findOneByUser(user)).thenReturn(token);

    getNotifier().createExpirationToken(user, repository, user -> token);

    // then
    verify(repository).findOneByUser(user);
    verify(repository).delete(token);
    verify(repository).flush();
  }

  void verifyNotificationRequest(User user, String subject, String content) {
    verify(notificationService).notify(user, subject, content);
  }
}
