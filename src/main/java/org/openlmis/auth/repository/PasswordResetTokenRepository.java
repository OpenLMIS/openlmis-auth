package org.openlmis.auth.repository;

import org.openlmis.auth.domain.PasswordResetToken;
import org.openlmis.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

  PasswordResetToken findOneByUser(@Param("user") User user);
}
