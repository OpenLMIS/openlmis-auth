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

package org.openlmis.auth.i18n;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

public class ExposedMessageSourceImpl
    extends ReloadableResourceBundleMessageSource
    implements ExposedMessageSource {

  protected Properties getAllProperties(Locale locale) {
    clearCacheIncludingAncestors();
    PropertiesHolder propertiesHolder = getMergedProperties(locale);
    return propertiesHolder.getProperties();
  }

  /**
   * Get all messages for given locale.
   *
   * @param locale Locale.
   * @return Map with messages.
   */
  public Map<String, String> getAllMessages(Locale locale) {
    Properties props = getAllProperties(locale);
    Enumeration<String> keys = (Enumeration<String>) props.propertyNames();
    Map<String, String> asMap = new HashMap<>();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      asMap.put(key, props.getProperty(key));
    }
    return asMap;
  }
}
