package org.openlmis.auth;

import static java.time.Clock.systemUTC;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;

import org.openlmis.auth.domain.ApiKey;

import java.util.UUID;

public class ApiKeyDataBuilder {
  private UUID id;
  private String clientId;
  private UUID serviceAccountId;

  public ApiKeyDataBuilder() {
    id = UUID.randomUUID();
    clientId = "api-key-client-" + now(systemUTC()).format(ofPattern("yyyyMMddHHmmssSSS"));
    serviceAccountId = UUID.randomUUID();
  }

  public ApiKey buildAsNew() {
    return new ApiKey(clientId, serviceAccountId);
  }

  public ApiKey build() {
    ApiKey key = buildAsNew();
    key.setId(id);

    return key;
  }
}
