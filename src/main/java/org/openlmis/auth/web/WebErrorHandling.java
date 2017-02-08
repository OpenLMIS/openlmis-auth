package org.openlmis.auth.web;

import org.openlmis.auth.exception.BindingResultException;
import org.openlmis.auth.exception.ValidationMessageException;
import org.openlmis.auth.i18n.Message;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Map;

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

  /**
   * Handles the {@link BindingResultException} which signals a validation problems.
   *
   * @return a map of binding result field and message
   */
  @ExceptionHandler(BindingResultException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public Map<String, String> handleBindingRequltException(BindingResultException ex) {
    return ex.getErrors();
  }

  /**
   * Handles the {@link ValidationMessageException} which signals a validation problems.
   *
   * @return the localized message
   */
  @ExceptionHandler(ValidationMessageException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public Message.LocalizedMessage handleBindingRequltException(ValidationMessageException ex) {
    return getLocalizedMessage(ex.asMessage());
  }

}
