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

import org.openlmis.auth.domain.User;
import org.openlmis.auth.dto.referencedata.UserDto;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import lombok.EqualsAndHashCode;

import java.util.UUID;

interface TestWebData {

  abstract class Fields {
    static final String GRANT_TYPE = "grant_type";
    static final String USERNAME = "username";
    static final String PASSWORD = "password";
    static final String USER_ID = "userId";
    static final String EMAIL = "email";
    static final String ACCESS_TOKEN = "access_token";
    static final String MESSAGE_KEY = "messageKey";
    static final String MESSAGE = "message";
    static final String SERVICE_ACCOUNT_ID = "serviceAccountId";

    private Fields() {
      throw new UnsupportedOperationException();
    }

  }

  abstract class GrantTypes {
    static final String PASSWORD = "password";

    private GrantTypes() {
      throw new UnsupportedOperationException();
    }

  }

  abstract class Tokens {
    static final String BEARER = "Bearer ";

    static final String USER_TOKEN = "418c89c5-7f21-4cd1-a63a-38c47892b0fe";
    static final String SERVICE_TOKEN = "6d6896a5-e94c-4183-839d-911bc63174ff";
    static final String API_KEY_TOKEN = "9b0708fc-e0ca-11e7-80c1-9a214cf093ae";

    static final int DURATION = 1000;

    private Tokens() {
      throw new UnsupportedOperationException();
    }
  }

  abstract class ClientIds {
    static final String USER_CLIENT_ID = "user-client";
    static final String SERVICE_CLIENT_ID = "trusted-client";
    static final String API_KEY_CLIENT_ID = "api-key-client-20171214111354128";

    private ClientIds() {
      throw new UnsupportedOperationException();
    }
  }

  final class DummyUserDto extends UserDto {
    static final String AUTH_ID = "51f6bdc1-4932-4bc3-9589-368646ef7ad3";
    static final String REFERENCE_ID = "35316636-6264-6331-2d34-3933322d3462";
    static final String USERNAME = "admin";
    static final String PASSWORD = "password";
    static final String EMAIL = "test@openlmis.org";

    @Override
    public UUID getId() {
      return UUID.fromString(REFERENCE_ID);
    }

    @Override
    public String getUsername() {
      return USERNAME;
    }

    @Override
    public String getFirstName() {
      return "Admin";
    }

    @Override
    public String getLastName() {
      return "User";
    }

    @Override
    public String getEmail() {
      return EMAIL;
    }

    @Override
    public boolean isVerified() {
      return false;
    }

  }

  @EqualsAndHashCode(callSuper = false)
  class DummyOAuth2Authentication extends OAuth2Authentication {
    private User user;

    DummyOAuth2Authentication(String clientId) {
      super(new OAuth2Request(null, clientId, null, true, null, null, null, null, null), null);
    }

    DummyOAuth2Authentication(String clientId, String username) {
      super(new OAuth2Request(null, clientId, null, true, null, null, null, null, null), null);
      this.user = new User();
      this.user.setUsername(username);
    }

    @Override
    public boolean isClientOnly() {
      return null == user;
    }

    @Override
    public Object getPrincipal() {
      return user;
    }
  }
}
