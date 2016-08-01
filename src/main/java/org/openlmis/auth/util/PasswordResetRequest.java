package org.openlmis.auth.util;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;

@NoArgsConstructor
public class PasswordResetRequest {

  @Getter
  @Setter
  @NotNull
  private String username;

  @Getter
  @Setter
  @NotNull
  @Size(min = 8, max = 16)
  @Pattern.List({
        @Pattern(regexp = "(?=.*[0-9]).+", message = "must contain at least 1 number"),
        @Pattern(regexp = "(?=\\S+$).+", message = "must not contain spaces")
      })
  private String newPassword;

  public PasswordResetRequest(String username, String newPassword) {
    this.username = username;
    this.newPassword = newPassword;
  }

}
