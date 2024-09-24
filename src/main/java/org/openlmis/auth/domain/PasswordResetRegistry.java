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

import java.time.ZonedDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "password_reset_registries")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PasswordResetRegistry extends BaseEntity {

  @OneToOne
  @JoinColumn(name = "userId", nullable = false, unique = true)
  private User user;

  @Column(nullable = false, columnDefinition = "timestamp with time zone")
  private ZonedDateTime lastAttemptDate = ZonedDateTime.now();

  @Column(nullable = false, columnDefinition = "timestamp with time zone")
  private ZonedDateTime lastCounterResetDate = ZonedDateTime.now();

  @Column(name = "attemptcounter")
  private Integer attemptCounter = 0;

  @Column(name = "blocked")
  private Boolean blocked;

  public PasswordResetRegistry (User user) {
    this.user = user;
  }

  public void resetCounter() {
    this.setAttemptCounter(0);
    this.setLastCounterResetDate(ZonedDateTime.now());
    this.setLastAttemptDate(ZonedDateTime.now());
  }

  public void incrementCounter() {
    this.setAttemptCounter(this.getAttemptCounter() + 1);
    this.setLastAttemptDate(ZonedDateTime.now());
  }

}
