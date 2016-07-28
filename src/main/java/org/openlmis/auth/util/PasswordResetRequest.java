package org.openlmis.auth.util;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
public class PasswordResetRequest {

  @Getter
  @Setter
  @NotNull
  private String username;

  @Getter
  @Setter
  @NotNull
  private String newPassword;

  public PasswordResetRequest(String username, String newPassword) {
    this.username = username;
    this.newPassword = newPassword;
  }

}
