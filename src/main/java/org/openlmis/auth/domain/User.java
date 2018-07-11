/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.auth.domain;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collection;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

@Entity
@Table(name = "auth_users")
@JsonIgnoreProperties(value = { "authorities" }, ignoreUnknown = true)
public class User extends BaseEntity implements UserDetails {
  private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

  @Getter
  @Setter
  @Column(nullable = false, unique = true)
  private String username;

  @Getter
  @Setter
  @Column
  private String password;

  @Getter
  @Setter
  @Column
  private Boolean enabled;

  /**
   * Creates new instance of {@link User} based on passed data.
   */
  public static User newInstance(Importer importer) {
    User user = new User();
    user.setId(importer.getId());
    user.updateFrom(importer);

    return user;
  }

  /**
   * Update user data from {@link Importer}.
   */
  public void updateFrom(Importer importer) {
    username = importer.getUsername();
    enabled = importer.getEnabled();

    String newPassword = importer.getPassword();
    if (StringUtils.hasText(newPassword)) {
      password = ENCODER.encode(newPassword);
    }
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return AuthorityUtils.createAuthorityList(UserRole.USER.name());
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return isTrue(getEnabled());
  }

  /**
   * Export data of this object to the exporter instance.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setUsername(username);
    exporter.setPassword(password);
    exporter.setEnabled(enabled);

  }

  public interface Importer {

    UUID getId();

    String getUsername();

    String getPassword();

    Boolean getEnabled();

  }

  public interface Exporter {

    void setId(UUID id);

    void setUsername(String username);

    void setPassword(String password);

    void setEnabled(Boolean enabled);

  }

}
