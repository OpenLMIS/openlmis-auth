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

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.auth.DummyUserMainDetailsDto;
import org.openlmis.auth.ToStringTestUtils;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.dto.referencedata.UserMainDetailsDto;

public class UserDtoTest {

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(UserDto.class)
        .withRedefinedSuperclass()
        .suppress(Warning.NONFINAL_FIELDS) // dto can't contain final fields
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ToStringTestUtils
        .verify(UserDto.class,
            new UserDto(new User(), new DummyUserMainDetailsDto()));
  }

  @Test
  public void shouldGetReferenceDataUser() {
    UserMainDetailsDto admin = new DummyUserMainDetailsDto();
    UserDto request = new UserDto(new User(), admin);

    assertThat(request.getReferenceDataUser()).isEqualTo(admin);
  }
}
