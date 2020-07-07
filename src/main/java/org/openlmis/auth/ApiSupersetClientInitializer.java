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

package org.openlmis.auth;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.auth.domain.Client;
import org.openlmis.auth.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(19)
public class ApiSupersetClientInitializer implements CommandLineRunner {

  @Autowired
  private ClientRepository clientRepository;

  @Value("${auth.server.clientId.superset}")
  private String supersetClientId;

  @Value("${auth.server.clientId.superset.secret}")
  private String supersetClientSecret;

  @Value("${auth.server.clientId.superset.redirectUri}")
  private String supersetClientRedirectUri;

  private static final String AUTHORIZED_GRANT_TYPES = "authorization_code";
  private static final String AUTHORITIES = "TRUSTED_CLIENT";
  private static final String SCOPE = "read,write";
  private static final String RESOURCE_IDS = "hapifhir,notification,diagnostics,cce,auth,"
          + "requisition,referencedata,report,stockmanagement,fulfillment,reference-ui";

  /**
   * This method is part of CommandLineRunner and is called automatically by Spring.
   * @param args Main method arguments.
   */
  public void run(String... args) {
    if (hasDefinedProperties()) {
      createClient();
    }
  }

  private void createClient() {
    Optional<Client> client = clientRepository.findOneByClientId(supersetClientId);

    if (client.isPresent() && clientSholudBeUpdated(client.get())) {
      client.get().setRegisteredRedirectUris(supersetClientRedirectUri);
      client.get().setClientSecret(supersetClientSecret);
      clientRepository.saveAndFlush(client.get());
    } else if (!client.isPresent()) {
      clientRepository.saveAndFlush(new Client(supersetClientId, supersetClientSecret, AUTHORITIES,
              supersetClientRedirectUri, AUTHORIZED_GRANT_TYPES, SCOPE, RESOURCE_IDS));
    }
  }

  private boolean clientSholudBeUpdated(Client client) {
    return !StringUtils.equals(client.getClientSecret(), supersetClientSecret)
            || !StringUtils.equals(client.getRegisteredRedirectUris(), supersetClientRedirectUri);
  }

  private boolean hasDefinedProperties() {
    return StringUtils.isNotBlank(supersetClientId)
            && StringUtils.isNotBlank(supersetClientSecret);
  }
}
