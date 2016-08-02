package org.openlmis.auth.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeRequest {

  @Getter
  @Setter
  private UUID token;

  @Getter
  @Setter
  private String newPassword;
}
