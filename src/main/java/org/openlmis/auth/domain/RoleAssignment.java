package org.openlmis.auth.domain;

import com.fasterxml.jackson.annotation.JsonView;

import org.openlmis.auth.exception.RightTypeException;
import org.openlmis.auth.util.View;

import java.util.Objects;
import java.util.Set;

public abstract class RoleAssignment extends BaseEntity {

  @JsonView(View.BasicInformation.class)
  protected Role role;

  protected User user;

  /**
   * Default constructor. Must always have a role.
   *
   * @param role the role being assigned
   * @throws RightTypeException if role passed in has rights which are not an acceptable right type
   */
  public RoleAssignment(Role role) throws RightTypeException {
    Set<RightType> acceptableRightTypes = getAcceptableRightTypes();
    boolean roleTypeAcceptable = acceptableRightTypes.stream()
        .anyMatch(rightType -> rightType == role.getRightType());
    if (!roleTypeAcceptable) {
      throw new RightTypeException("referencedata.error.type-not-in-acceptable-types");
    }

    this.role = role;
  }

  protected abstract Set<RightType> getAcceptableRightTypes();

  public abstract boolean hasRight(RightQuery rightQuery);

  public void assignTo(User user) {
    this.user = user;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RoleAssignment)) {
      return false;
    }
    RoleAssignment that = (RoleAssignment) obj;
    return Objects.equals(role, that.role)
        && Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(role, user);
  }
}
