/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.auth.web;

import static org.openlmis.auth.i18n.MessageKeys.ERROR_SEND_REQUEST;

import org.openlmis.auth.exception.BindingResultException;
import org.openlmis.auth.exception.PermissionMessageException;
import org.openlmis.auth.exception.ValidationMessageException;
import org.openlmis.auth.util.Message;
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
            ERROR_SEND_REQUEST,
            ex.getStatusCode().toString(), ex.getResponseBodyAsString()
        )
    );
  }

  /**
   * Handles the {@link PermissionMessageException} which signals unauthorized access.
   *
   * @return the localized message
   */
  @ExceptionHandler(PermissionMessageException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ResponseBody
  public Message.LocalizedMessage handlePermissionMessageException(PermissionMessageException ex) {
    return getLocalizedMessage(ex.asMessage());
  }

  /**
   * Handles the {@link BindingResultException} which signals a validation problems.
   *
   * @return a map of binding result field and message
   */
  @ExceptionHandler(BindingResultException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public Map<String, String> handleBindingResultException(BindingResultException ex) {
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
  public Message.LocalizedMessage handleValidationMessageException(ValidationMessageException ex) {
    return getLocalizedMessage(ex.asMessage());
  }

}
