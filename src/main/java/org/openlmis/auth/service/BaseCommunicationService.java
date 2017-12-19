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

import static org.openlmis.auth.util.RequestHelper.createEntity;
import static org.openlmis.auth.util.RequestHelper.createUri;

import org.openlmis.auth.dto.ResultDto;
import org.openlmis.auth.util.DynamicPageTypeReference;
import org.openlmis.auth.util.DynamicResultDtoTypeReference;
import org.openlmis.auth.util.PageImplRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("PMD.TooManyMethods")
public abstract class BaseCommunicationService<T> {
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected RestOperations restTemplate = new RestTemplate();

  @Autowired
  private AccessTokenService accessTokenService;

  @Value("${auth.server.clientId}")
  private String clientId;

  protected abstract String getServiceUrl();

  protected abstract String getUrl();

  protected abstract Class<T> getResultClass();

  protected abstract Class<T[]> getArrayResultClass();

  protected String obtainAccessToken() {
    return accessTokenService.obtainToken(clientId);
  }

  /**
   * Return one object from service.
   *
   * @param id UUID of requesting object.
   * @return Requesting reference data object.
   */
  public T findOne(UUID id) {
    return findOne(id.toString(), RequestParameters.init());
  }

  /**
   * Return one object from service.
   *
   * @param resourceUrl Endpoint url.
   * @param parameters  Map of query parameters.
   * @return one reference data T objects.
   */
  public T findOne(String resourceUrl, RequestParameters parameters) {
    return findOne(resourceUrl, parameters, getResultClass());
  }

  /**
   * Return one object from service.
   *
   * @param resourceUrl Endpoint url.
   * @param parameters  Map of query parameters.
   * @param type        set to what type a response should be converted.
   * @return one reference data T objects.
   */
  public <P> P findOne(String resourceUrl, RequestParameters parameters, Class<P> type) {
    String url = getServiceUrl() + getUrl() + resourceUrl;

    RequestParameters params = RequestParameters
            .init()
            .setAll(parameters);

    try {
      return restTemplate
              .exchange(createUri(url, params),
                      HttpMethod.GET,
                      createEntity(obtainAccessToken()),
                      type)
              .getBody();
    } catch (HttpStatusCodeException ex) {
      // rest template will handle 404 as an exception, instead of returning null
      if (HttpStatus.NOT_FOUND == ex.getStatusCode()) {
        logger.warn(
                "{} matching params does not exist. Params: {}",
                getResultClass().getSimpleName(), parameters
        );

        return null;
      }

      throw buildDataRetrievalException(ex);
    }
  }

  /**
   * Return all reference data T objects.
   *
   * @param resourceUrl Endpoint url.
   * @param parameters  Map of query parameters.
   * @return all reference data T objects.
   */
  public List<T> findAll(String resourceUrl, RequestParameters parameters) {
    return findAll(resourceUrl, parameters, getArrayResultClass());
  }

  public <P> List<P> findAll(String resourceUrl, RequestParameters parameters, Class<P[]> type) {
    return findAll(resourceUrl, parameters, null, HttpMethod.GET, type);
  }

  /**
   * Return all reference data T objects that need to be retrieved with POST request.
   *
   * @param resourceUrl Endpoint url.
   * @param parameters  Map of query parameters.
   * @param payload     body to include with the outgoing request.
   * @return all reference data T objects.
   */
  protected List<T> findAll(String resourceUrl, RequestParameters parameters,
                            Object payload) {
    return findAll(resourceUrl, parameters, payload, HttpMethod.POST, getArrayResultClass());
  }

  protected <P> List<P> findAll(String resourceUrl, RequestParameters parameters,
                                Object payload, HttpMethod method, Class<P[]> type) {
    String url = getServiceUrl() + getUrl() + resourceUrl;

    RequestParameters params = RequestParameters
        .init()
        .setAll(parameters);

    try {
      ResponseEntity<P[]> response = restTemplate.exchange(
              createUri(url, params),
              method,
              createEntity(obtainAccessToken(), payload),
              type
      );

      return Stream.of(response.getBody()).collect(Collectors.toList());
    } catch (HttpStatusCodeException ex) {
      throw buildDataRetrievalException(ex);
    }
  }

  protected T put(String resourceUrl, T instance) {
    String url = getServiceUrl() + getUrl() + resourceUrl;

    try {
      ResponseEntity<T> response = restTemplate.exchange(
          createUri(url),
          HttpMethod.PUT,
          createEntity(obtainAccessToken(), instance),
          getResultClass()
      );
      return response.getBody();

    } catch (HttpStatusCodeException ex) {
      throw buildDataRetrievalException(ex);
    }
  }

  protected <P> ResultDto<P> getResult(String resourceUrl, RequestParameters parameters,
                                       Class<P> type) {
    String url = getServiceUrl() + getUrl() + resourceUrl;
    RequestParameters params = RequestParameters
        .init()
        .setAll(parameters);

    try {
      ResponseEntity<ResultDto<P>> response = restTemplate.exchange(
          createUri(url, params),
          HttpMethod.GET,
          createEntity(obtainAccessToken()),
          new DynamicResultDtoTypeReference<>(type)
      );
      return response.getBody();

    } catch (HttpStatusCodeException ex) {
      throw buildDataRetrievalException(ex);
    }
  }

  protected Page<T> getPage(String resourceUrl, Map<String, Object> parameters) {
    return getPage(resourceUrl, parameters, null, HttpMethod.GET, getResultClass());
  }

  /**
   * Return all reference data T objects for Page that need to be retrieved with POST request.
   *
   * @param resourceUrl Endpoint url.
   * @param parameters  Map of query parameters.
   * @param payload     body to include with the outgoing request.
   * @return Page of reference data T objects.
   */
  protected Page<T> getPage(String resourceUrl, Map<String, Object> parameters, Object payload) {
    return getPage(resourceUrl, parameters, payload, HttpMethod.POST, getResultClass());
  }

  protected <P> Page<P> getPage(String resourceUrl, Map<String, Object> parameters, Object payload,
                                HttpMethod method, Class<P> type) {
    String url = getServiceUrl() + getUrl() + resourceUrl;

    Map<String, Object> params = new HashMap<>();
    params.putAll(parameters);

    try {
      ResponseEntity<PageImplRepresentation<P>> response = restTemplate.exchange(
              buildUri(url, params),
              method,
              createEntity(obtainAccessToken(), payload),
              new DynamicPageTypeReference<>(type)
      );
      return response.getBody();

    } catch (HttpStatusCodeException ex) {
      throw buildDataRetrievalException(ex);
    }
  }

  private DataRetrievalException buildDataRetrievalException(HttpStatusCodeException ex) {
    return new DataRetrievalException(getResultClass().getSimpleName(),
        ex.getStatusCode(),
        ex.getResponseBodyAsString());
  }

  protected URI buildUri(String url, Map<String, ?> params) {
    UriComponentsBuilder builder = UriComponentsBuilder.newInstance().uri(URI.create(url));

    params.forEach(builder::queryParam);

    return builder.build(true).toUri();
  }

  void setRestTemplate(RestOperations template) {
    this.restTemplate = template;
  }

  void setAccessTokenService(AccessTokenService accessTokenService) {
    this.accessTokenService = accessTokenService;
  }
}
