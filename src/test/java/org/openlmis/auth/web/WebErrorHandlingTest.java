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

package org.openlmis.auth.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_CONSTRAINT;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_SEND_REQUEST;

import com.google.common.collect.ImmutableMap;
import java.util.Locale;
import java.util.Map;
import javax.persistence.PersistenceException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.auth.dto.LocalizedMessageDto;
import org.openlmis.auth.exception.BindingResultException;
import org.openlmis.auth.exception.ExternalApiException;
import org.openlmis.auth.exception.NotFoundException;
import org.openlmis.auth.exception.PermissionMessageException;
import org.openlmis.auth.exception.ServerException;
import org.openlmis.auth.exception.ValidationMessageException;
import org.openlmis.auth.i18n.MessageService;
import org.openlmis.auth.util.Message;
import org.openlmis.auth.util.Message.LocalizedMessage;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

@RunWith(MockitoJUnitRunner.class)
public class WebErrorHandlingTest {
  private static final Locale ENGLISH_LOCALE = Locale.ENGLISH;
  private static final String MESSAGE_KEY = "key";
  private static final String ERROR_MESSAGE = "error-message";

  @Mock
  private MessageService messageService;

  @Mock
  private MessageSource messageSource;

  @InjectMocks
  private WebErrorHandling errorHandler;

  @Before
  public void setUp() {
    when(messageService.localize(any(Message.class)))
        .thenAnswer(invocation -> {
          Message message = invocation.getArgumentAt(0, Message.class);
          return message.localMessage(messageSource, ENGLISH_LOCALE);
        });
  }

  @Test
  public void shouldHandleHttpStatusCodeException() {
    // given
    HttpStatusCodeException exp = mock(HttpStatusCodeException.class);
    when(exp.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
    when(exp.getResponseBodyAsString()).thenReturn("body");

    // when
    mockMessage(ERROR_SEND_REQUEST, "400", "body");
    LocalizedMessage message = errorHandler.handleHttpStatusCodeException(exp);

    // then
    assertMessage(message, ERROR_SEND_REQUEST);
  }

  @Test
  public void shouldHandlePermissionMessageException() {
    // given
    PermissionMessageException exp = new PermissionMessageException(MESSAGE_KEY);

    // when
    mockMessage(MESSAGE_KEY);
    LocalizedMessage message = errorHandler.handlePermissionMessageException(exp);

    // then
    assertMessage(message, MESSAGE_KEY);
  }

  @Test
  public void shouldHandleBindingResultException() {
    // given
    BindingResultException exp = mock(BindingResultException.class);
    when(exp.getErrors()).thenReturn(ImmutableMap.of(MESSAGE_KEY, "value"));

    // when
    mockMessage(MESSAGE_KEY);
    Map<String, String> errors = errorHandler.handleBindingResultException(exp);

    // then
    assertThat(errors).containsEntry(MESSAGE_KEY, "value");
  }

  @Test
  public void shouldHandleValidationMessageException() {
    // given
    ValidationMessageException exp = new ValidationMessageException(new Message(MESSAGE_KEY));

    // when
    mockMessage(MESSAGE_KEY);
    LocalizedMessage message = errorHandler.handleValidationMessageException(exp);

    // then
    assertMessage(message, MESSAGE_KEY);
  }

  @Test
  public void shouldHandleNotFoundException() {
    // given
    NotFoundException exp = new NotFoundException(new Message(MESSAGE_KEY));

    // when
    mockMessage(MESSAGE_KEY);
    LocalizedMessage message = errorHandler.handleNotFoundException(exp);

    // then
    assertMessage(message, MESSAGE_KEY);
  }

  @Test
  public void shouldHandlePersistenceException() {
    // given
    PersistenceException exp = mock(PersistenceException.class);

    // when
    mockMessage(ERROR_CONSTRAINT);
    LocalizedMessage message = errorHandler.handlePersistenceException(exp);

    // then
    assertMessage(message, ERROR_CONSTRAINT);
  }

  @Test
  public void shouldHandleServerException() {
    // given
    ServerException exp = new ServerException(null, MESSAGE_KEY);

    // when
    mockMessage(MESSAGE_KEY);
    LocalizedMessage message = errorHandler.handleServerException(exp);

    // then
    assertMessage(message, MESSAGE_KEY);
  }

  @Test
  public void shouldHandleExternalApiException() {
    // given
    ExternalApiException exp = new ExternalApiException(
        null, new LocalizedMessageDto(MESSAGE_KEY, null)
    );

    // when
    LocalizedMessageDto message = errorHandler.handleExternalApiException(exp);

    // then
    assertThat(message)
        .hasFieldOrPropertyWithValue("messageKey", MESSAGE_KEY);
  }

  private void assertMessage(LocalizedMessage localized, String key) {
    assertThat(localized)
        .hasFieldOrPropertyWithValue("messageKey", key);
    assertThat(localized)
        .hasFieldOrPropertyWithValue("message", ERROR_MESSAGE);
  }

  private void mockMessage(String key, String... params) {
    if (params.length == 0) {
      when(messageSource.getMessage(key, new Object[0], ENGLISH_LOCALE))
          .thenReturn(ERROR_MESSAGE);
      when(messageSource.getMessage(key, null, ENGLISH_LOCALE))
          .thenReturn(ERROR_MESSAGE);
    } else {
      when(messageSource.getMessage(key, params, ENGLISH_LOCALE))
          .thenReturn(ERROR_MESSAGE);
    }
  }

}
