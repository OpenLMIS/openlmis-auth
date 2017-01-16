package org.openlmis.auth.web;

import org.openlmis.auth.i18n.Message;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Controller advice responsible for handling errors from web layer.
 */
@ControllerAdvice
public class WebErrorHandling extends AbstractErrorHandling {

  /**
   * Handles the {@link HttpStatusCodeException} which signals a problems with sending a request.
   *
   * @return the localized message
   */
  @ExceptionHandler(HttpStatusCodeException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public Message.LocalizedMessage handleHttpStatusCodeException(HttpStatusCodeException ex) {
    return logErrorAndRespond(
        "Unable to send a request", ex, new Message(
            "error.sendRequest",
            ex.getStatusCode().toString(), ex.getResponseBodyAsString()
        )
    );
  }

}
