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

import static org.openlmis.auth.i18n.MessageKeys.ERROR_CLIENT_NOT_FOUND;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_TOKEN_INVALID;

import org.apache.commons.codec.binary.Base64;
import org.openlmis.auth.domain.Client;
import org.openlmis.auth.exception.NotFoundException;
import org.openlmis.auth.exception.ValidationMessageException;
import org.openlmis.auth.repository.ClientRepository;
import org.openlmis.auth.service.AccessTokenService;
import org.openlmis.auth.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@Transactional
@RequestMapping("/api")
public class ApiKeyController {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyController.class);
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
      .ofPattern("yyyyMMddHHmmssSSS");

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private TokenStore tokenStore;

  @Autowired
  private AccessTokenService accessTokenService;

  @Autowired
  private ClientRepository clientRepository;

  @Value("${auth.apiKey.clientId.prefix}")
  private String clientIdPrefix;

  /**
   * Creates new API Key.
   */
  @RequestMapping(value = "/apiKeys", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public String createApiKey() {
    Profiler profiler = new Profiler("CREATE_API_KEY");
    profiler.setLogger(LOGGER);

    profiler.start("CHECK_PERMISSION");
    permissionService.canManageApiKeys();

    profiler.start("GET_CURRENT_DATE");
    LocalDateTime currentDate = LocalDateTime.now(Clock.systemUTC());
    String currentDateString = currentDate.format(DATE_TIME_FORMATTER);

    profiler.start("CREATE_CLIENT");
    String clientId = clientIdPrefix + currentDateString;
    String clientSecret = new String(Base64.encodeBase64(clientId.getBytes()));

    Client client = new Client(
        clientId, clientSecret, "TRUSTED_CLIENT", "client_credentials", "read,write", 0
    );
    clientRepository.save(client);

    profiler.start("OBTAIN_TOKEN");
    String token = accessTokenService.obtainToken(client.getClientId(), client.getClientSecret());

    profiler.stop().log();
    return token;
  }

  /**
   * Removes provided API Key.
   */
  @RequestMapping(value = "/apiKeys/{key}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeApiKey(@PathVariable("key") String key) {
    Profiler profiler = new Profiler("REMOVE_API_KEY");
    profiler.setLogger(LOGGER);

    profiler.start("CHECK_PERMISSION");
    permissionService.canManageApiKeys();

    profiler.start("READ_AUTHENTICATION");
    OAuth2Authentication authentication = tokenStore.readAuthentication(key);

    if (null == authentication) {
      throw new ValidationMessageException(ERROR_TOKEN_INVALID);
    }

    String clientId = authentication.getOAuth2Request().getClientId();

    profiler.start("FIND_CLIENT");
    final Client client = clientRepository
        .findOneByClientId(clientId)
        .orElseThrow(() -> new NotFoundException(ERROR_CLIENT_NOT_FOUND));

    profiler.start("REMOVE_CLIENT");
    clientRepository.delete(client);

    profiler.start("REMOVE_ACCESS_TOKEN");
    tokenStore.removeAccessToken(new DefaultOAuth2AccessToken(key));

    profiler.stop().log();
  }

}
