package org.openlmis.auth.repository;

import org.openlmis.auth.domain.Client;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientRepository extends CrudRepository<Client, String> {

  Optional<Client> findOneByClientId(@Param("clientId") String clientId);
}
