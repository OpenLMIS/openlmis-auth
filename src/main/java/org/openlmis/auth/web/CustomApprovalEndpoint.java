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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.oauth2.provider.endpoint.WhitelabelApprovalEndpoint;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for displaying the approval page for the authorization server. Other than using
 * url prefix, it doesn't differ from the default one.
 */
@RestController
@SessionAttributes("authorizationRequest")
public class CustomApprovalEndpoint extends WhitelabelApprovalEndpoint {
  private static final String PREFIX = "/api";

  /**
   * Displays approval page.
   */
  @RequestMapping("/api/oauth/confirm_access")
  public ModelAndView getAccessConfirmation(Map<String, Object> model, HttpServletRequest request)
      throws Exception {
    String template = createTemplate(model, request)
        .replace("/oauth/authorize", PREFIX + "/oauth/authorize");
    if (request.getAttribute("_csrf") != null) {
      model.put("_csrf", request.getAttribute("_csrf"));
    }
    return new ModelAndView(new SpelView(template), model);
  }
}
