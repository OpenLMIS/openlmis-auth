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

package org.openlmis.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.auth.UserDataBuilder;
import org.openlmis.auth.dto.UserDto;

public class UserTest {

  private static final String ID_FIELD = "id";
  private static final String PASSWORD_FIELD = "password";
  private static final String ENCODER_FIELD = "ENCODER";
  private static final String UUID_TYPE_FIELD = "UUID_TYPE";

  private UserDto importer;
  private User user = new UserDataBuilder().withPassword("oldPassword").build();

  @Before
  public void setUp() {
    importer = new UserDto();
    importer.setUsername("someUserName");
    importer.setId(UUID.randomUUID());
    importer.setPassword("password");
    importer.setEnabled(true);
  }

  @Test
  public void shouldCreateNewInstanceFromImporter() {
    User user = User.newInstance(importer);
    assertThat(user).isEqualToIgnoringGivenFields(
        importer, PASSWORD_FIELD, ENCODER_FIELD, UUID_TYPE_FIELD);
  }

  @Test
  public void shouldUpdateFromImporter() {
    user.updateFrom(importer);

    assertThat(user).isEqualToIgnoringGivenFields(
        importer, ID_FIELD, PASSWORD_FIELD, ENCODER_FIELD, UUID_TYPE_FIELD);
    assertThat(user.getPassword()).isNotEqualTo("oldPassword");
  }

  @Test
  public void shouldNotChangePasswordIfImporterDoesNotHaveIt() {
    importer.setPassword(null);

    user.updateFrom(importer);

    assertThat(user).isEqualToIgnoringGivenFields(
        importer, ID_FIELD, PASSWORD_FIELD, ENCODER_FIELD, UUID_TYPE_FIELD);
    assertThat(user.getPassword()).isEqualTo("oldPassword");
  }

  @Test
  public void shouldExportData() {
    UserDto exporter = new UserDto();
    user.export(exporter);

    assertThat(exporter).isEqualToIgnoringGivenFields(user, ENCODER_FIELD, UUID_TYPE_FIELD);
  }

  @Test
  public void shouldAlwaysReturnOnlyUserRoleForGetAuthorities() {
    assertThat(user.getAuthorities()).extracting("authority").contains(UserRole.USER.name());
  }

  @Test
  public void shouldReturnTrueForIsAccountNonExpired() {
    assertThat(user.isAccountNonExpired()).isTrue();
  }

  @Test
  public void shouldReturnTrueForIsAccountNonLocked() {
    assertThat(user.isAccountNonLocked()).isTrue();

  }

  @Test
  public void shouldReturnTrueForIsCredentialsNonExpired() {
    assertThat(user.isCredentialsNonExpired()).isTrue();
  }

  @Test
  public void shouldReturnTrueForIsEnabled() {
    user.setEnabled(true);
    assertThat(user.isEnabled()).isTrue();
  }

  @Test
  public void shouldReturnFalseForIsEnabled() {
    user.setEnabled(false);
    assertThat(user.isEnabled()).isFalse();

    user.setEnabled(null);
    assertThat(user.isEnabled()).isFalse();
  }
}
