package org.openlmis.auth.service;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class HttpContextHelper {

  private HttpContextHelper() {
    throw new UnsupportedOperationException();
  }

  /**
   * Static method returning current HTTP Request.
   *
   * @return HttpServletRequest
   */
  static HttpServletRequest getCurrentHttpRequest() {
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

    return requestAttributes instanceof ServletRequestAttributes
            ? ((ServletRequestAttributes) requestAttributes).getRequest() : null;
  }
}
