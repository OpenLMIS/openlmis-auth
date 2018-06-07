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

package org.openlmis.auth.repository;

import java.util.UUID;
import org.junit.Test;
import org.openlmis.auth.EmailVerificationTokenDataBuilder;
import org.openlmis.auth.UserDataBuilder;
import org.openlmis.auth.domain.EmailVerificationToken;
import org.openlmis.auth.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;

public class EmailVerificationTokenRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<EmailVerificationToken> {

  @Autowired
  private EmailVerificationTokenRepository repository;

  @Autowired
  private UserRepository userRepository;

  @Override
  CrudRepository<EmailVerificationToken, UUID> getRepository() {
    return repository;
  }

  @Override
  EmailVerificationToken generateInstance() {
    User user = new UserDataBuilder().build();
    userRepository.save(user);

    return new EmailVerificationTokenDataBuilder()
        .withoutId()
        .withUser(user)
        .build();
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldBePossibleToHaveOnlyOneTokenPerUser() {
    EmailVerificationToken token = generateInstance();
    repository.save(token);

    EmailVerificationToken newToken = new EmailVerificationTokenDataBuilder()
        .withoutId()
        .withUser(token.getUser())
        .build();
    repository.saveAndFlush(newToken);
  }

}
