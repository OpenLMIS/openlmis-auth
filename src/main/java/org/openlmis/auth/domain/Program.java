package org.openlmis.auth.domain;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.Getter;
import lombok.Setter;

import org.openlmis.auth.util.View;

public class Program extends BaseEntity {

  @JsonView(View.BasicInformation.class)
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
  private Boolean active;
}
