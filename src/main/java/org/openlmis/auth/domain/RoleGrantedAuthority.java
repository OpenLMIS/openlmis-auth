package org.openlmis.auth.domain;


import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import org.openlmis.auth.util.JsonRawValueDeserializer;

public class RoleGrantedAuthority implements org.springframework.security.core.GrantedAuthority {

  @Getter
  @Setter
  @JsonRawValue
  @JsonDeserialize(using = JsonRawValueDeserializer.class)
  private String authority;

  public RoleGrantedAuthority(ClientRole role) {
    this.authority = role.toString();
  }

}
