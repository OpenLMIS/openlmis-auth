package org.openlmis.auth.domain;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.Getter;
import lombok.Setter;

import org.openlmis.auth.util.View;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings({"PMD.UnusedPrivateField"})
public class Right extends BaseEntity {

  @JsonView(View.BasicInformation.class)
  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private RightType type;

  @Getter
  @Setter
  private String description;

  @Getter
  private Set<Right> attachments = new HashSet<>();

  public Right(String name, RightType type) {
    this.name = name;
    this.type = type;
  }

  /**
   * Constructor for name, type, description.
   *
   * @param name        right name
   * @param type        right type
   * @param description right description
   */
  public Right(String name, RightType type, String description) {
    this.name = name;
    this.type = type;
    this.description = description;
  }

  /**
   * Attach other rights to this one, to create relationships between rights. The attachment is
   * one-way with this method call. The attached rights must be of the same type; only attachments
   * of the same type are attached.
   *
   * @param attachments the rights being attached
   */
  public void attach(Right... attachments) {
    for (Right attachment : attachments) {
      if (attachment.type == type) {
        this.attachments.add(attachment);
      }
    }
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Right)) {
      return false;
    }
    Right right = (Right) obj;
    return Objects.equals(name, right.name);
  }
}