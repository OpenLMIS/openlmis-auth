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

package org.openlmis.auth.util;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.net.URI;
import org.junit.Test;
import org.openlmis.auth.service.RequestParameters;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public class RequestHelperTest {

  private static final String URL = "http://localhost";

  @Test
  public void shouldCreateUriWithoutParameters() throws Exception {
    URI uri = RequestHelper.createUri(URL, RequestParameters.init());
    assertThat(uri.getQuery(), is(nullValue()));
  }

  @Test
  public void shouldCreateUriWithNullParameters() throws Exception {
    URI uri = RequestHelper.createUri(URL);
    assertThat(uri.getQuery(), is(nullValue()));
  }

  @Test
  public void shouldCreateUriWithParameters() throws Exception {
    URI uri = RequestHelper.createUri(URL, RequestParameters.init().set("a", "b"));
    assertThat(uri.getQuery(), is("a=b"));
  }

  @Test
  public void shouldCreateUriWithIncorrectParameters() throws Exception {
    URI uri = RequestHelper.createUri(URL, RequestParameters.init().set("a", "b c"));
    assertThat(uri.getQuery(), is("a=b c"));
  }

  @Test
  public void shouldCreateEntityWithAnAuthHeader() {
    String body = "test";
    String token = "token";

    HttpEntity<String> entity = RequestHelper.createEntity(token, body);

    assertThat(entity.getHeaders().get(HttpHeaders.AUTHORIZATION),
            is(singletonList("Bearer " + token)));
    assertThat(entity.getBody(), is(body));
  }

  @Test
  public void shouldCreateEntityWithNoBody() {
    String token = "token";

    HttpEntity<String> entity = RequestHelper.createEntity(token, null);

    assertThat(entity.getHeaders().get(HttpHeaders.AUTHORIZATION),
            is(singletonList("Bearer " + token)));
  }
}
