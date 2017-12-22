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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Maps;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;
import org.openlmis.auth.ApiKeyDataBuilder;
import org.openlmis.auth.ToStringTestUtils;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

public class ApiKeyTest {

  @Test
  public void shouldCreateInstanceBasedOnImporter() {
    ApiKey expected = new ApiKeyDataBuilder().build();
    ApiKey.Importer importer = new ApiKey.Importer() {

      @Override
      public UUID getToken() {
        return expected.getToken();
      }

      @Override
      public UUID getCreatedBy() {
        return expected.getCreationDetails().getCreatedBy();
      }

      @Override
      public ZonedDateTime getCreatedDate() {
        return expected.getCreationDetails().getCreatedDate();
      }
    };

    ApiKey actual = ApiKey.newApiKey(importer);
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void shouldExportValues() {
    Map<String, Object> values = Maps.newHashMap();
    ApiKey.Exporter exporter = new ApiKey.Exporter() {

      @Override
      public void setToken(UUID apiKey) {
        values.put("apiKey", apiKey);
      }

      @Override
      public void setCreatedBy(UUID createdBy) {
        values.put("createdBy", createdBy);
      }

      @Override
      public void setCreatedDate(ZonedDateTime createdDate) {
        values.put("createdDate", createdDate);
      }
    };

    ApiKey key = new ApiKeyDataBuilder().build();
    key.export(exporter);

    assertThat(values, hasEntry("apiKey", key.getToken()));
    assertThat(values, hasEntry("createdBy", key.getCreationDetails().getCreatedBy()));
    assertThat(values, hasEntry("createdDate", key.getCreationDetails().getCreatedDate()));
  }

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(ApiKey.class)
        .withRedefinedSuperclass()
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ApiKey account = new ApiKeyDataBuilder().build();
    ToStringTestUtils.verify(ApiKey.class, account);
  }

}
