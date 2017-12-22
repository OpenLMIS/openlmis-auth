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

import static org.openlmis.auth.i18n.MessageKeys.ERROR_API_KEY_NOT_FOUND;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_CLIENT_NOT_FOUND;
import static org.openlmis.auth.i18n.MessageKeys.ERROR_TOKEN_INVALID;

import org.apache.commons.codec.binary.Base64;
import org.openlmis.auth.domain.ApiKey;
import org.openlmis.auth.domain.Client;
import org.openlmis.auth.domain.CreationDetails;
import org.openlmis.auth.dto.ApiKeyDto;
import org.openlmis.auth.dto.referencedata.UserDto;
import org.openlmis.auth.exception.NotFoundException;
import org.openlmis.auth.exception.ValidationMessageException;
import org.openlmis.auth.repository.ApiKeyRepository;
import org.openlmis.auth.repository.ClientRepository;
import org.openlmis.auth.service.AccessTokenService;
import org.openlmis.auth.service.ApiKeySettings;
import org.openlmis.auth.service.PermissionService;
import org.openlmis.auth.service.consul.ConsulCommunicationService;
import org.openlmis.auth.util.AuthenticationHelper;
import org.openlmis.auth.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@Transactional
@RequestMapping("/api")
public class ApiKeyController {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyController.class);

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private TokenStore tokenStore;

  @Autowired
  private AccessTokenService accessTokenService;

  @Autowired
  private ClientRepository clientRepository;

  @Autowired
  private ConsulCommunicationService consulCommunicationService;

  @Autowired
  private ApiKeyRepository apiKeyRepository;

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private ApiKeySettings apiKeySettings;

  /**
   * Creates new API Key.
   */
  @RequestMapping(value = "/apiKeys", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public ApiKeyDto createApiKey() {
    Profiler profiler = new Profiler("CREATE_API_KEY");
    profiler.setLogger(LOGGER);

    canManageApiKeys(profiler);

    profiler.start("GET_CURRENT_USER");
    final UserDto user = authenticationHelper.getCurrentUser();

    profiler.start("CREATE_CLIENT");
    String clientId = apiKeySettings.generateClientId();
    String clientSecret = new String(Base64.encodeBase64(clientId.getBytes()));

    Client client = new Client(
        clientId, clientSecret, "TRUSTED_CLIENT", "client_credentials", "read,write", 0
    );

    clientRepository.saveAndFlush(client);

    profiler.start("UPDATE_OAUTH_RESOURCES");
    consulCommunicationService.updateOAuthResources();

    profiler.start("OBTAIN_TOKEN");
    UUID token = accessTokenService.obtainToken(client.getClientId());

    profiler.start("CREATE_NEW_INSTANCE");
    ApiKey key = new ApiKey(token, new CreationDetails(user.getId()));
    apiKeyRepository.save(key);

    profiler.start("EXPORT_API_KEY_TO_DTO");
    ApiKeyDto dto = ApiKeyDto.newInstance(key);

    profiler.stop().log();

    return dto;
  }

  /**
   * Retrieves all API Keys.
   *
   * @return Page of API Keys.
   */
  @RequestMapping(value = "/apiKeys", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<ApiKeyDto> getApiKeys(Pageable pageable) {
    Profiler profiler = new Profiler("GET_API_KEYS");
    profiler.setLogger(LOGGER);

    canManageApiKeys(profiler);

    profiler.start("DB_CALL");
    Page<ApiKey> result = apiKeyRepository.findAll(pageable);

    profiler.start("EXPORT_API_KEYS_TO_DTO");
    List<ApiKeyDto> dtos = result.getContent()
        .stream()
        .map(ApiKeyDto::newInstance)
        .collect(Collectors.toList());

    profiler.start("CREATE_PAGE");
    List<ApiKeyDto> subList = Pagination.getPage(dtos, pageable).getContent();
    Page<ApiKeyDto> page = Pagination.getPage(subList, pageable, result.getTotalElements());

    profiler.stop().log();

    return page;
  }

  /**
   * Removes provided API Key.
   */
  @RequestMapping(value = "/apiKeys/{token}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeApiKey(@PathVariable("token") UUID token) {
    Profiler profiler = new Profiler("REMOVE_API_KEY");
    profiler.setLogger(LOGGER);

    canManageApiKeys(profiler);

    profiler.start("FIND_API_KEY");
    ApiKey apiKey = apiKeyRepository.findOne(token);

    if (null == apiKey) {
      throw new NotFoundException(ERROR_API_KEY_NOT_FOUND);
    }

    profiler.start("READ_AUTHENTICATION");
    OAuth2Authentication authentication = tokenStore.readAuthentication(token.toString());

    if (null == authentication) {
      throw new ValidationMessageException(ERROR_TOKEN_INVALID);
    }

    String clientId = authentication.getOAuth2Request().getClientId();

    profiler.start("FIND_CLIENT");
    final Client client = clientRepository
        .findOneByClientId(clientId)
        .orElseThrow(() -> new NotFoundException(ERROR_CLIENT_NOT_FOUND));

    profiler.start("REMOVE_ACCESS_TOKEN");
    tokenStore.removeAccessToken(new DefaultOAuth2AccessToken(token.toString()));

    profiler.start("REMOVE_CLIENT");
    clientRepository.delete(client);

    profiler.start("REMOVE_API_KEY");
    apiKeyRepository.delete(apiKey);

    profiler.stop().log();
  }

  private void canManageApiKeys(Profiler profiler) {
    profiler.start("CHECK_PERMISSION");
    permissionService.canManageApiKeys();
  }

}
