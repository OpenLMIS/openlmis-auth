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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class UserDetailsServiceImplTest {
  private static final String USERNAME = "username";

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserDetailsServiceImpl userDetailsService;

  @Test
  public void shouldLoadUserByUsername() {
    // given
    User expected = new User();

    // when
    when(userRepository.findOneByUsernameIgnoreCase(USERNAME)).thenReturn(expected);
    UserDetails actual = userDetailsService.loadUserByUsername(USERNAME);

    // then
    assertThat(actual).isEqualTo(expected);
  }

  @Test(expected = UsernameNotFoundException.class)
  public void shouldThrowExceptionIfUserCannotBeFound() {
    when(userRepository.findOneByUsernameIgnoreCase(USERNAME)).thenReturn(null);
    userDetailsService.loadUserByUsername(USERNAME);
  }
}
