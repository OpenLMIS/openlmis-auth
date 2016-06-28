package org.openlmis.auth.domain;

public enum Scope {
  READ, WRITE, Scope;

  @Override
  public String toString() {
    return super.toString().toLowerCase();
  }
}
