package org.openlmis.auth.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * RequisitionGroup represents a group of facilities which follow a particular schedule for a
 * program. It also defines the contract for creation/upload of RequisitionGroup.
 */
public class RequisitionGroup extends BaseEntity {

  @Getter
  @Setter
  private String code;

  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private String description;

  @Getter
  @Setter
  private SupervisoryNode supervisoryNode;

  @Getter
  @Setter
  private List<Facility> memberFacilities;

  private RequisitionGroup(SupervisoryNode supervisoryNode, List<Facility> memberFacilities) {
    this.supervisoryNode = supervisoryNode;
    this.memberFacilities = memberFacilities;
  }

  /**
   * Create a new requisition group with a specified supervisory node, program schedules and
   * facilities.
   *
   * @param supervisoryNode  specified supervisory node
   * @param memberFacilities specified facilities
   * @return the new requisition group
   */
  public static RequisitionGroup newRequisitionGroup(SupervisoryNode supervisoryNode,
                                                     List<Facility> memberFacilities) {
    RequisitionGroup newRequisitionGroup = new RequisitionGroup(supervisoryNode, memberFacilities);

    return newRequisitionGroup;
  }
}
