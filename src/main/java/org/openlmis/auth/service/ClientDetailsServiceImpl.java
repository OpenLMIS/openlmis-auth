package org.openlmis.auth.service;

import org.openlmis.auth.domain.Client;
import org.openlmis.auth.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.stereotype.Service;

@Service
@Primary
public class ClientDetailsServiceImpl implements ClientDetailsService {

  @Autowired
  private ClientRepository clientRepository;

  @Override
  public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
    Client client = clientRepository.findOneByClientId(clientId)
        .orElseThrow(() -> new NoSuchClientException(
            String.format("Client with clientId=%s was not found", clientId)));

    return new org.openlmis.auth.domain.ClientDetails(client);
  }

}
