package org.openlmis.auth.domain;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.Getter;
import lombok.Setter;

import org.openlmis.auth.exception.RightTypeException;
import org.openlmis.auth.util.View;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Role extends BaseEntity {

  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private String description;

  @JsonView(View.BasicInformation.class)
  private Set<Right> rights;

  /**
   * Role constructor with name and rights.
   *
   * @param name   the role name
   * @param rights the rights to group
   * @throws RightTypeException if the rights do not have the same right type
   */
  public Role(String name, Right... rights) throws RightTypeException {
    this.name = name;
    group(rights);
  }

  /**
   * Role constructor with name, description and rights.
   *
   * @param name        the role name
   * @param description the role description
   * @param rights      the rights to group
   * @throws RightTypeException if the rights do not have the same right type
   */
  public Role(String name, String description, Right... rights) throws RightTypeException {
    this.name = name;
    this.description = description;
    group(rights);
  }

  /**
   * Group rights together and assign to this role. These rights replace any previously existing
   * rights.
   *
   * @param rights the rights to group
   * @throws RightTypeException if the rights do not have the same right type
   */
  public void group(Right... rights) throws RightTypeException {
    Set<Right> rightsList = new HashSet<>(asList(rights));
    if (checkRightTypesMatch(rightsList)) {
      this.rights = rightsList;
    } else {
      throw new RightTypeException("referencedata.error.rights-are-different-types");
    }
  }

  public RightType getRightType() {
    return rights.iterator().next().getType();
  }

  private static boolean checkRightTypesMatch(Set<Right> rightSet) throws RightTypeException {
    if (rightSet.isEmpty()) {
      return true;
    } else {
      RightType rightType = rightSet.iterator().next().getType();
      return rightSet.stream().allMatch(right -> right.getType() == rightType);
    }
  }

  /**
   * Add additional rights to the role.
   *
   * @param additionalRights the rights to add
   * @throws RightTypeException if the resulting rights do not have the same right type
   */
  public void add(Right... additionalRights) throws RightTypeException {
    Set<Right> allRights = concat(rights.stream(), asList(additionalRights).stream())
        .collect(toSet());

    if (checkRightTypesMatch(allRights)) {
      rights.addAll(Arrays.asList(additionalRights));
    } else {
      throw new RightTypeException("referencedata.error.rights-are-different-types");
    }
  }

  /**
   * Check if the role contains a specified right. Attached rights are also checked, but only one
   * level down and it is assumed that the attached rights structure is a "tree" with no loops.
   *
   * @param right the right to check
   * @return true if the role contains the right, false otherwise
   */
  public boolean contains(Right right) {
    Set<Right> attachments = rights.stream().flatMap(r -> r.getAttachments().stream())
        .collect(toSet());
    return rights.contains(right) || attachments.contains(right);
  }
}
