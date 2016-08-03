package org.openlmis.auth.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken extends BaseEntity {

  @Getter
  @Setter
  @Column(nullable = false)
  private LocalDateTime expiryDate;

  @Getter
  @Setter
  @OneToOne
  @JoinColumn(nullable = false, unique = true)
  private User user;
}
