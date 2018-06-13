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
import static org.openlmis.auth.i18n.MessageKeys.EMAIL_VERIFICATION_EMAIL_BODY;
import static org.openlmis.auth.i18n.MessageKeys.EMAIL_VERIFICATION_EMAIL_SUBJECT;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.auth.SaveAnswer;
import org.openlmis.auth.domain.EmailVerificationToken;
import org.openlmis.auth.repository.EmailVerificationTokenRepository;
import org.openlmis.auth.repository.ExpirationTokenRepository;

public class EmailVerificationNotifierTest
    extends ExpirationTokenNotifierTest<EmailVerificationToken> {

  @Mock
  private EmailVerificationTokenRepository emailVerificationTokenRepository;

  @InjectMocks
  private EmailVerificationNotifier notifier;

  @Captor
  private ArgumentCaptor<EmailVerificationToken> tokenCaptor;

  private String email = "example@test.org";

  @Override
  @Before
  public void setUp() {
    super.setUp();

    when(emailVerificationTokenRepository.findOneByUser(user)).thenReturn(null);
    when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
        .thenAnswer(new SaveAnswer<>());
  }

  @Override
  ExpirationTokenNotifier<EmailVerificationToken> getNotifier() {
    return notifier;
  }

  @Override
  ExpirationTokenRepository<EmailVerificationToken> getRepository() {
    return emailVerificationTokenRepository;
  }

  @Override
  EmailVerificationToken getToken() {
    return new EmailVerificationToken();
  }

  @Test
  public void shouldSendNotification() {
    // when
    notifier.sendNotification(user, email);

    // then
    verify(emailVerificationTokenRepository).save(tokenCaptor.capture());
    verifyNotificationRequest(
        email, EMAIL_VERIFICATION_EMAIL_SUBJECT, EMAIL_VERIFICATION_EMAIL_BODY
    );

    EmailVerificationToken token = tokenCaptor.getValue();
    assertThat(token.getUser()).isEqualTo(user);
    assertThat(token.getEmail()).isEqualTo(email);
  }
}
