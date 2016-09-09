package org.openlmis.auth.domain;

import static java.util.Arrays.asList;

import org.openlmis.auth.exception.RightTypeException;

import java.util.HashSet;
import java.util.Set;

public class DirectRoleAssignment extends RoleAssignment {
  public DirectRoleAssignment(Role role) throws RightTypeException {
    super(role);
  }

  @Override
  protected Set<RightType> getAcceptableRightTypes() {
    return new HashSet<>(asList(RightType.GENERAL_ADMIN, RightType.REPORTS));
  }

  @Override
  public boolean hasRight(RightQuery rightQuery) {
    return role.contains(rightQuery.getRight());
  }

  @Override
  public void assignTo(User user) {
    super.assignTo(user);
  }
}
