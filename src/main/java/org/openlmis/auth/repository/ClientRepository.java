package org.openlmis.auth.repository;

import org.openlmis.auth.domain.Client;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Optional;

@PreAuthorize("hasAuthority('ADMIN')")
public interface ClientRepository extends CrudRepository<Client, String> {

  @PreAuthorize("permitAll()")
  Optional<Client> findOneByClientId(@Param("clientId") String clientId);
}
