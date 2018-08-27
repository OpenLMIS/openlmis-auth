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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.ModelAndView;

public class DefaultContentTypeInterceptorTest {
  private DefaultContentTypeInterceptor interceptor = new DefaultContentTypeInterceptor();
  private HttpServletRequest request;
  private HttpServletResponse response;
  private Object handler;
  private ModelAndView modelAndView;
  private Exception exception;

  @Before
  public void setUp() {
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    handler = mock(Object.class);
    modelAndView = mock(ModelAndView.class);
    exception = mock(Exception.class);
  }

  @Test
  public void shouldReturnTrueForPreHandle() {
    assertThat(interceptor.preHandle(request, response, handler)).isTrue();
    verifyZeroInteractions(request, response, handler);
  }

  @Test
  public void shouldSetContentTypeIfResponseDoesNotHaveIt() {
    when(response.getContentType()).thenReturn(null);

    interceptor.postHandle(request, response, handler, modelAndView);

    verify(response).setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
    verifyZeroInteractions(request, handler, modelAndView);
  }

  @Test
  public void shouldNotSetContentTypeIfResponseHasIt() {
    when(response.getContentType()).thenReturn(MediaType.TEXT_PLAIN_VALUE);

    interceptor.postHandle(request, response, handler, modelAndView);

    verify(response, never()).setContentType(anyString());
    verifyZeroInteractions(request, handler, modelAndView);
  }

  @Test
  public void shouldDoNothingInAfterCompletionMethod() {
    interceptor.afterCompletion(request, response, handler, exception);
    verifyZeroInteractions(request, response, handler, exception);
  }

}
