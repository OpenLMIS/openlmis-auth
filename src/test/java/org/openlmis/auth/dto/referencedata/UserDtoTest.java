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

package org.openlmis.auth.dto.referencedata;

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.auth.DummyUserDto;
import org.openlmis.auth.ToStringTestUtils;
import org.openlmis.auth.dto.UserSaveRequest;

public class UserDtoTest {
  private UserDto user = new DummyUserDto();

  @Test
  public void shouldHasEmail() {
    assertThat(user.hasEmail()).isTrue();
  }

  @Test
  public void shouldNotHasEmail() {
    user.setEmail(null);
    assertThat(user.hasEmail()).isFalse();
  }

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(UserDto.class)
        .withRedefinedSuperclass()
        .withRedefinedSubclass(UserSaveRequest.class)
        .suppress(Warning.NONFINAL_FIELDS) // dto can't contain final fields
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ToStringTestUtils.verify(UserDto.class, user);
  }

}
