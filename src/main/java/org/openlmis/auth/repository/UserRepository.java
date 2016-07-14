package org.openlmis.auth.repository;

import org.openlmis.auth.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Optional;
import java.util.UUID;

@PreAuthorize("hasAuthority('ADMIN')")
public interface UserRepository extends CrudRepository<User, UUID> {

  @PreAuthorize("permitAll()")
  Optional<User> findOneByUsername(String username);
}
