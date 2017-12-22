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

package org.openlmis.auth.dto;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;
import org.openlmis.auth.ApiKeyDataBuilder;
import org.openlmis.auth.ToStringTestUtils;
import org.openlmis.auth.domain.ApiKey;

public class ApiKeyDtoTest {

  @Test
  public void shouldCreateInstanceBasedOnDomain() {
    ApiKey account = new ApiKeyDataBuilder().build();
    ApiKeyDto dto = ApiKeyDto.newInstance(account);

    assertThat(dto.getToken(), is(equalTo(account.getToken())));
    assertThat(dto.getCreatedBy(), is(equalTo(account.getCreationDetails().getCreatedBy())));
    assertThat(dto.getCreatedDate(), is(equalTo(account.getCreationDetails().getCreatedDate())));
  }

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(ApiKeyDto.class)
        .withRedefinedSuperclass()
        .suppress(Warning.NONFINAL_FIELDS) // DTO fields cannot be final
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ApiKeyDto dto = ApiKeyDto.newInstance(
        new ApiKeyDataBuilder().build()
    );

    ToStringTestUtils.verify(ApiKeyDto.class, dto);
  }

}
