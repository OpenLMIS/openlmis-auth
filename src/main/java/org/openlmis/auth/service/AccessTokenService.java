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

package org.openlmis.auth.service;

import org.apache.commons.codec.binary.Base64;
import org.openlmis.auth.util.RequestHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

@Service
public class AccessTokenService {
  static final String ACCESS_TOKEN = "access_token";
  static final String AUTHORIZATION = "Authorization";

  @Value("${auth.server.authorizationUrl}")
  private String authorizationUrl;

  private RestOperations restTemplate = new RestTemplate();

  /**
   * Obtains token based on client ID and secret.
   */
  public String obtainToken(String clientId, String clientSecret) {
    String plainCreds = clientId + ":" + clientSecret;
    byte[] plainCredsBytes = plainCreds.getBytes();
    byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
    String base64Creds = new String(base64CredsBytes);

    HttpHeaders headers = new HttpHeaders();
    headers.add(AUTHORIZATION, "Basic " + base64Creds);

    HttpEntity<String> request = new HttpEntity<>(headers);

    RequestParameters parameters = RequestParameters
        .init()
        .set("grant_type", "client_credentials");

    URI url = RequestHelper.createUri(authorizationUrl, parameters);
    ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

    return String.valueOf(response.getBody().get(ACCESS_TOKEN));
  }

}
