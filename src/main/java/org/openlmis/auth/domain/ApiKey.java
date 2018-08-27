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
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "api_keys")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
public final class ApiKey implements Identifiable {

  @Id
  @Column(nullable = false, unique = true)
  private UUID token;

  @OneToOne
  @JoinColumn(name = "clientid", nullable = false)
  private Client client;

  @Embedded
  private CreationDetails creationDetails;

  @Override
  public UUID getId() {
    return token;
  }

  /**
   * Exports current state of service account object.
   *
   * @param exporter instance of {@link Exporter}
   */
  public void export(Exporter exporter) {
    exporter.setToken(token);
    exporter.setCreatedBy(creationDetails.getCreatedBy());
    exporter.setCreatedDate(creationDetails.getCreatedDate());
  }

  public interface Exporter {

    void setToken(UUID apiKey);

    void setCreatedBy(UUID createdBy);

    void setCreatedDate(ZonedDateTime createdDate);

  }

  public interface Importer {

    UUID getToken();

    UUID getCreatedBy();

    ZonedDateTime getCreatedDate();

  }
}
