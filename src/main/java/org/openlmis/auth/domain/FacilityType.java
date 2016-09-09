package org.openlmis.auth.domain;

import lombok.Getter;
import lombok.Setter;

public class FacilityType extends BaseEntity {

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
  private Integer displayOrder;

  @Getter
  @Setter
  private Boolean active;

  public FacilityType(String code) {
    this.code = code;
  }
}
