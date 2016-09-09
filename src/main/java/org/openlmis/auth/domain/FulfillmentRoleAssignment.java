package org.openlmis.auth.domain;

import static java.util.Collections.singleton;

import com.fasterxml.jackson.annotation.JsonView;

import org.openlmis.auth.exception.RightTypeException;
import org.openlmis.auth.exception.RoleAssignmentException;
import org.openlmis.auth.util.View;

import java.util.Objects;
import java.util.Set;

public class FulfillmentRoleAssignment extends RoleAssignment {

  @JsonView(View.BasicInformation.class)
  private Facility warehouse;

  private FulfillmentRoleAssignment(Role role) throws RightTypeException {
    super(role);
  }

  /**
   * Default constructor. Must always have a role and a facility, which must be of type
   * 'warehouse'.
   *
   * @param role      the role being assigned
   * @param warehouse the warehouse where the role applies
   * @throws RightTypeException      if role passed in has rights which are not an acceptable right
   *                                 type
   * @throws RoleAssignmentException if facility passed in is not of type 'warehouse'
   */
  public FulfillmentRoleAssignment(Role role, Facility warehouse)
      throws RightTypeException, RoleAssignmentException {
    super(role);

    if (!warehouse.getType().getCode().equalsIgnoreCase("warehouse")) {
      throw new RoleAssignmentException("referencedata.error.facility-type-must-be-warehouse");
    }

    this.warehouse = warehouse;
  }

  @Override
  protected Set<RightType> getAcceptableRightTypes() {
    return singleton(RightType.ORDER_FULFILLMENT);
  }

  @Override
  public boolean hasRight(RightQuery rightQuery) {
    boolean roleMatches = role.contains(rightQuery.getRight());
    boolean warehouseMatches = warehouse.equals(rightQuery.getWarehouse());

    return roleMatches && warehouseMatches;
  }

  @Override
  public void assignTo(User user) {
    super.assignTo(user);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FulfillmentRoleAssignment)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    FulfillmentRoleAssignment that = (FulfillmentRoleAssignment) obj;
    return Objects.equals(role, that.role)
        && Objects.equals(user, that.user)
        && Objects.equals(warehouse, that.warehouse);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), warehouse);
  }
}
