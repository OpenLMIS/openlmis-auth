package org.openlmis.auth.repository;

import org.openlmis.auth.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
  Optional<Client> findOneByClientId(String clientId);
}
