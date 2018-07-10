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
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.auth.service.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.auth.DummyUserMainDetailsDto;
import org.openlmis.auth.dto.PageDto;
import org.openlmis.auth.dto.referencedata.UserMainDetailsDto;
import org.openlmis.auth.service.BaseCommunicationService;
import org.openlmis.auth.service.BaseCommunicationServiceTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

public class UserContactDetailsNotificationServiceTest extends BaseCommunicationServiceTest {

  UserContactDetailsNotificationService service;

  @Override
  protected BaseCommunicationService getService() {
    return new UserContactDetailsNotificationService();
  }

  @Override
  protected UserContactDetailsNotificationService prepareService() {
    BaseCommunicationService service = super.prepareService();

    ReflectionTestUtils.setField(service, "serviceUrl", "http://localhost/notification");

    return (UserContactDetailsNotificationService) service;
  }

  @Before
  public void before() {
    service = prepareService();
  }

  @Test
  public void shouldFindByEmail() {
    // given
    EmailDetailsDto emailDetails = new EmailDetailsDto(DummyUserMainDetailsDto.EMAIL, true);

    UserContactDetailsDto contactDetails = new UserContactDetailsDto();
    contactDetails.setEmailDetails(emailDetails);

    Map<String, Object> payload = new HashMap<>();
    payload.put("email", DummyUserMainDetailsDto.EMAIL);

    ResponseEntity response = mock(ResponseEntity.class);

    // when
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
        any(ParameterizedTypeReference.class)))
        .thenReturn(response);

    PageDto<UserContactDetailsDto> page = new PageDto<>(new PageImpl<>(ImmutableList.of(
        contactDetails)));

    when(response.getBody()).thenReturn(page);

    List<UserContactDetailsDto> contacts = service.findByEmail(DummyUserMainDetailsDto.EMAIL);
    UserContactDetailsDto contact = contacts.get(0);

    // then
    verify(restTemplate).exchange(uriCaptor.capture(), eq(HttpMethod.GET),
        entityCaptor.capture(), any(ParameterizedTypeReference.class));

    URI uri = uriCaptor.getValue();
    String url = service.getServiceUrl() + service.getUrl();

    assertThat(uri.toString())
        .containsSequence(url)
        .containsSequence("email=" + DummyUserMainDetailsDto.EMAIL);
    assertThat(contact.getEmailDetails().getEmail()).isEqualTo(DummyUserMainDetailsDto.EMAIL);

    assertAuthHeader(entityCaptor.getValue());
    assertThat(entityCaptor.getValue().getBody()).isNull();
  }

  @Test
  public void shouldReturnEmptyListIfContactDetailsCannotBeFoundByEmail() {
    // given
    ResponseEntity response = mock(ResponseEntity.class);

    Map<String, Object> payload = new HashMap<>();
    payload.put("email", DummyUserMainDetailsDto.EMAIL);

    // when
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
        any(ParameterizedTypeReference.class)))
        .thenReturn(response);

    PageDto<UserMainDetailsDto> page = new PageDto<>(new PageImpl<>(Collections.emptyList()));
    when(response.getBody()).thenReturn(page);

    List<UserContactDetailsDto> contacts = service.findByEmail(DummyUserMainDetailsDto.EMAIL);

    // then
    verify(restTemplate).exchange(uriCaptor.capture(), eq(HttpMethod.GET),
        entityCaptor.capture(), any(ParameterizedTypeReference.class));

    URI uri = uriCaptor.getValue();
    String url = service.getServiceUrl() + service.getUrl();

    assertThat(uri.toString())
        .containsSequence(url)
        .containsSequence("email=" + DummyUserMainDetailsDto.EMAIL);
    assertThat(contacts).isEmpty();

    assertAuthHeader(entityCaptor.getValue());
    assertThat(entityCaptor.getValue().getBody()).isNull();
  }

}
