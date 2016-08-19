package org.openlmis.auth.repository;

import org.openlmis.auth.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends CrudRepository<User, UUID> {

  Optional<User> findOneByUsername(@Param("username") String username);

  User findOneByEmail(@Param("email") String email);

  User findOneByReferenceDataUserId(@Param("referenceDataUserId") UUID referenceDataUserId);
}
