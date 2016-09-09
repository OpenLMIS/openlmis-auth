package org.openlmis.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.Type;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "auth_users")
@JsonIgnoreProperties(value = {"authorities"}, ignoreUnknown = true)
@SuppressWarnings({"PMD.UnusedPrivateField"})
public class User extends BaseEntity implements UserDetails {

  @Getter
  @Setter
  @Type(type = "pg-uuid")
  @Column(nullable = false, unique = true)
  private UUID referenceDataUserId;

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
  @Column(nullable = false, unique = true)
  private String email;

  @Getter
  @Setter
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private UserRole role;

  @Getter
  @Setter
  @Column
  private Boolean enabled;

  @Transient
  @Getter
  private Set<RoleAssignment> roleAssignments = new HashSet<>();

  @Transient
  private Facility homeFacility;

  @Transient
  private Set<Program> homeFacilityPrograms = new HashSet<>();

  @Transient
  private Set<Program> supervisedPrograms = new HashSet<>();

  @Transient
  private Set<Facility> supervisedFacilities = new HashSet<>();

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return AuthorityUtils.createAuthorityList(this.getRole().name(), UserRole.USER.name());
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
    return this.getEnabled();
  }

  /**
   * Add role assignments to this user. Also puts a link to user within each role assignment.
   *
   * @param roleAssignments role assignments to add
   */
  public void assignRoles(RoleAssignment... roleAssignments) {
    for (RoleAssignment roleAssignment : Arrays.asList(roleAssignments)) {
      roleAssignment.assignTo(this);
      this.roleAssignments.add(roleAssignment);
    }
  }

  public boolean hasRight(RightQuery rightQuery) {
    return roleAssignments.stream().anyMatch(roleAssignment -> roleAssignment.hasRight(rightQuery));
  }
  
  public Set<Program> getHomeFacilityPrograms() {
    return homeFacilityPrograms;
  }

  public void addHomeFacilityProgram(Program program) {
    homeFacilityPrograms.add(program);
  }

  public Set<Program> getSupervisedPrograms() {
    return supervisedPrograms;
  }

  public void addSupervisedProgram(Program program) {
    supervisedPrograms.add(program);
  }

  public Set<Facility> getSupervisedFacilities() {
    return supervisedFacilities;
  }

  public void addSupervisedFacilities(Set<Facility> facilities) {
    supervisedFacilities.addAll(facilities);
  }
}
