package org.openlmis.auth.web;

import org.openlmis.auth.i18n.Message;
import org.openlmis.auth.i18n.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base classes for controller advices dealing with error handling.
 */
abstract class AbstractErrorHandling {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private MessageService messageService;

  Message.LocalizedMessage logErrorAndRespond(String msg, Exception ex, Message message) {
    logger.info(msg, ex);
    return getLocalizedMessage(message);
  }

  private Message.LocalizedMessage getLocalizedMessage(Message message) {
    return messageService.localize(message);
  }

}
