package org.openlmis.auth.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SupervisoryNode extends BaseEntity {
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
  private Facility facility;

  @JsonIdentityInfo(
      generator = ObjectIdGenerators.IntSequenceGenerator.class,
      property = "parentId")
  @Getter
  @Setter
  private SupervisoryNode parentNode;

  @JsonIdentityInfo(
      generator = ObjectIdGenerators.IntSequenceGenerator.class,
      property = "childNodesSetId")
  @Getter
  @Setter
  private Set<SupervisoryNode> childNodes;

  @Getter
  @Setter
  private RequisitionGroup requisitionGroup;

  private SupervisoryNode(Facility facility) {
    this.facility = facility;
    this.childNodes = new HashSet<>();
    this.requisitionGroup = RequisitionGroup.newRequisitionGroup(this, new ArrayList<>());
  }

  /**
   * Create a new supervisory node.
   *
   * @param facility facility associated with this supervisory node
   * @return a new SupervisoryNode
   */
  public static SupervisoryNode newSupervisoryNode(Facility facility) {
    SupervisoryNode newSupervisoryNode = new SupervisoryNode(facility);

    return newSupervisoryNode;
  }

  /**
   * Add a child supervisory node to this one. Will also set this node as parent to child node.
   *
   * @param childNode child supervisory node to add.
   * @return true if added, false if it's already added or was otherwise unable to add.
   */
  public boolean addChildNode(SupervisoryNode childNode) {
    boolean added = childNodes.add(childNode);

    if (added) {
      childNode.setParentNode(this);
    }

    return added;
  }

  /**
   * Get all facilities being supervised by this supervisory node. Note, this does not get the
   * facility attached to this supervisory node. "All supervised facilities" means all facilities
   * supervised by this node and all recursive child nodes.
   *
   * @return all supervised facilities
   */
  public Set<Facility> getAllSupervisedFacilities() {
    Set<Facility> supervisedFacilities = new HashSet<>();

    if (requisitionGroup != null && requisitionGroup.getMemberFacilities() != null) {
      supervisedFacilities.addAll(requisitionGroup.getMemberFacilities());
    }

    if (childNodes != null) {
      for (SupervisoryNode childNode : childNodes) {
        supervisedFacilities.addAll(childNode.getAllSupervisedFacilities());
      }
    }

    return supervisedFacilities;
  }

  /**
   * Set requisition group for this supervisory node. It also sets this node as the supervisory node
   * for the requisition group specified.
   *
   * @param requisitionGroup specified requisition group
   */
  public void assignRequisitionGroup(RequisitionGroup requisitionGroup) {
    requisitionGroup.setSupervisoryNode(this);
    this.requisitionGroup = requisitionGroup;
  }
}
