package org.openlmis.auth.exception;

import org.openlmis.auth.i18n.Message;

public class ValidationMessageException extends RuntimeException {
  private final Message message;

  public ValidationMessageException(String messageKey) {
    this.message = new Message(messageKey);
  }

  public Message asMessage() {
    return message;
  }
}
