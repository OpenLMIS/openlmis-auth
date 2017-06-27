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

package org.openlmis.auth.service.consul;

import com.google.common.collect.Sets;

import org.openlmis.auth.domain.Client;
import org.openlmis.auth.dto.consul.ServicesListDto;
import org.openlmis.auth.repository.ClientRepository;
import org.openlmis.auth.service.ConfigurationSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import lombok.AccessLevel;
import lombok.Setter;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
class ConsulCommunicationService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsulCommunicationService.class);
  static final String SERVICE_SEPARATOR = ",";

  @Setter(AccessLevel.PACKAGE)
  private RestOperations restTemplate = new RestTemplate();

  @Autowired
  private ClientRepository clientRepository;

  @Autowired
  private ConfigurationSettingService configurationSettingService;

  @Scheduled(fixedRate = 60 * 1000)
  @Transactional
  public void updateOAuthResources() {
    LOGGER.info("Updating OAuth resources...");

    Set<String> services = getAvailableServices();
    String servicesString = String.join(",", services);
    Iterable<Client> clients = clientRepository.findAll();

    for (Client client : clients) {
      String resourceIds = Optional.ofNullable(client.getResourceIds()).orElse("");
      Set<String> clientServices = Sets.newHashSet(resourceIds.split(SERVICE_SEPARATOR));

      if (!services.equals(clientServices)) {
        client.setResourceIds(servicesString);
        clientRepository.save(client);
        LOGGER.info("Updated resources for %s: %s", client.getClientId(), servicesString);
      }
    }

    LOGGER.info("Finished updating OAuth resources.");
  }

  private Set<String> getAvailableServices() {
    ResponseEntity<ServicesListDto> response = restTemplate.getForEntity(
        getConsulServicesUrl(),
        ServicesListDto.class
    );

    ServicesListDto services = response.getBody();
    String serviceTag = configurationSettingService.getConsulServiceTag();

    return services.keySet()
        .stream()
        .filter(service -> services.isTagged(service, serviceTag))
        .collect(Collectors.toSet());
  }

  private String getConsulServicesUrl() {
    String protocol = configurationSettingService.getConsulProtocol();
    String host = configurationSettingService.getConsulHost();
    int port = configurationSettingService.getConsulPort();
    String services = configurationSettingService.getConsulServicesUrl();

    return String.format("%s://%s:%d%s", protocol, host, port, services);
  }
}
