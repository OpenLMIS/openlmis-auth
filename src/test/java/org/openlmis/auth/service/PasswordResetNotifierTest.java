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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.auth.i18n.MessageKeys.PASSWORD_RESET_EMAIL_BODY;
import static org.openlmis.auth.i18n.MessageKeys.PASSWORD_RESET_EMAIL_SUBJECT;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.auth.SaveAnswer;
import org.openlmis.auth.domain.PasswordResetToken;
import org.openlmis.auth.repository.ExpirationTokenRepository;
import org.openlmis.auth.repository.PasswordResetTokenRepository;

public class PasswordResetNotifierTest extends ExpirationTokenNotifierTest<PasswordResetToken> {

  @Mock
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @InjectMocks
  private PasswordResetNotifier notifier;

  @Captor
  private ArgumentCaptor<PasswordResetToken> tokenCaptor;

  private String email = "example@test.org";

  @Override
  @Before
  public void setUp() {
    super.setUp();

    when(passwordResetTokenRepository.findOneByUser(user)).thenReturn(null);
    when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
        .thenAnswer(new SaveAnswer<>());
  }

  @Override
  ExpirationTokenNotifier<PasswordResetToken> getNotifier() {
    return notifier;
  }

  @Override
  ExpirationTokenRepository<PasswordResetToken> getRepository() {
    return passwordResetTokenRepository;
  }

  @Override
  PasswordResetToken getToken() {
    return new PasswordResetToken();
  }

  @Test
  public void shouldSendNotification() {
    // when
    notifier.sendNotification(user, email);

    // then
    verify(passwordResetTokenRepository).save(tokenCaptor.capture());
    verifyNotificationRequest(
        user, PASSWORD_RESET_EMAIL_SUBJECT, PASSWORD_RESET_EMAIL_BODY
    );

    PasswordResetToken token = tokenCaptor.getValue();
    assertThat(token.getUser()).isEqualTo(user);
  }

}
