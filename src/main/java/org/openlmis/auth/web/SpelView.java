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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Simple String template renderer.
 *
 */
class SpelView implements View {

  private final String template;

  private final String prefix;

  private final SpelExpressionParser parser = new SpelExpressionParser();

  private final StandardEvaluationContext context = new StandardEvaluationContext();

  private PlaceholderResolver resolver;

  public SpelView(String template) {
    this.template = template;
    this.prefix = new RandomValueStringGenerator().generate() + "{";
    this.context.addPropertyAccessor(new MapAccessor());
    this.resolver = name -> {
      Expression expression = parser.parseExpression(name);
      Object value = expression.getValue(context);
      return value == null ? null : value.toString();
    };
  }

  public String getContentType() {
    return "text/html";
  }

  public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    Map<String, Object> map = new HashMap<String, Object>(model);
    String path = ServletUriComponentsBuilder.fromContextPath(request).build()
        .getPath();
    map.put("path", path == null ? "" : path);
    context.setRootObject(map);
    String maskedTemplate = template.replace("${", prefix);
    PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper(prefix, "}");
    String result = helper.replacePlaceholders(maskedTemplate, resolver);
    result = result.replace(prefix, "${");
    response.setContentType(getContentType());
    response.getWriter().append(result);
  }

}
