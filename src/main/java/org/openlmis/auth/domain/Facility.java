package org.openlmis.auth.domain;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.Getter;
import lombok.Setter;

import org.openlmis.auth.util.View;

import java.util.List;

public class Facility extends BaseEntity {

  @JsonView(View.BasicInformation.class)
  @Getter
  @Setter
  private String code;

  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private FacilityType type;

  @Getter
  @Setter
  private Boolean active;

  @Getter
  @Setter
  private Boolean enabled;

  @Getter
  @Setter
  private List<Program> supportedPrograms;
}
