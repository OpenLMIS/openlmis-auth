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

import java.util.Objects;
import org.openlmis.auth.i18n.ExposedMessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

abstract class BaseValidator implements Validator {

  @Autowired
  private ExposedMessageSource messageSource;

  void rejectIfNotEqual(Errors errors, Object oldData, Object newData, String field,
      String messageKey) {
    if (!Objects.equals(oldData, newData)) {
      rejectValue(errors, field, messageKey);
    }
  }

  void rejectIfEmptyOrWhitespace(Errors errors, String field, String messageKey) {
    ValidationUtils
        .rejectIfEmptyOrWhitespace(errors, field, messageKey, getMessage(field, messageKey));
  }

  void rejectValue(Errors errors, String field, String messageKey) {
    errors.rejectValue(field, messageKey, getMessage(field, messageKey));
  }

  private String getMessage(String field, String messageKey) {
    return messageSource
        .getMessage(messageKey, new Object[]{field}, LocaleContextHolder.getLocale());
  }

}
