package org.openlmis.auth.domain;


import lombok.Getter;
import lombok.Setter;

public class RoleGrantedAuthority implements org.springframework.security.core.GrantedAuthority {

  @Getter
  @Setter
  private String authority;

  public RoleGrantedAuthority(ClientRole role) {
    this.authority = role.toString();
  }

}
