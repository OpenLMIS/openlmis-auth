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

package org.openlmis.auth.security;

import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

public class CustomTokenStore extends JdbcTokenStore {
  // The following SQL statement are from super class.
  // Here we only modify them to match our database column names
  private static final String INSERT_ACCESS_TOKEN_SQL =
      "insert into auth.oauth_access_token (tokenid, token, authenticationid, username, clientid, "
          + "authentication, refreshtoken) values (?, ?, ?, ?, ?, ?, ?)";
  private static final String SELECT_ACCESS_TOKEN_SQL =
      "select tokenid, token from auth.oauth_access_token where tokenid = ?";
  private static final String SELECT_ACCESS_TOKEN_AUTHENTICATION_SQL =
      "select tokenid, authentication from auth.oauth_access_token where tokenid = ?";
  private static final String SELECT_ACCESS_TOKEN_FROM_AUTHENTICATION_SQL =
      "select tokenid, token from auth.oauth_access_token where authenticationid = ?";
  private static final String SELECT_ACCESS_TOKENS_FROM_USER_NAME_AND_CLIENT_ID_SQL =
      "select tokenid, token from auth.oauth_access_token where username = ? and clientid = ?";
  private static final String SELECT_ACCESS_TOKENS_FROM_USER_NAME_SQL =
      "select tokenid, token from auth.oauth_access_token where username = ?";
  private static final String SELECT_ACCESS_TOKENS_FROM_CLIENT_ID_SQL =
      "select tokenid, token from auth.oauth_access_token where clientid = ?";
  private static final String DELETE_ACCESS_TOKEN_SQL =
      "delete from auth.oauth_access_token where tokenid = ?";
  private static final String INSERT_REFRESH_TOKEN_SQL =
      "insert into auth.oauth_refresh_token (tokenid, token, authentication) values (?, ?, ?)";
  private static final String SELECT_REFRESH_TOKEN_SQL =
      "select tokenid, token from auth.oauth_refresh_token where tokenid = ?";
  private static final String SELECT_REFRESH_TOKEN_AUTHENTICATION_SQL =
      "select tokenid, authentication from auth.oauth_refresh_token where tokenid = ?";
  private static final String DELETE_REFRESH_TOKEN_SQL =
      "delete from auth.oauth_refresh_token where tokenid = ?";
  private static final String DELETE_ACCESS_TOKEN_FROM_REFRESH_TOKEN_SQL =
      "delete from auth.oauth_access_token where refreshtoken = ?";

  CustomTokenStore(DataSource dataSource) {
    super(dataSource);

    setInsertAccessTokenSql(INSERT_ACCESS_TOKEN_SQL);
    setSelectAccessTokenSql(SELECT_ACCESS_TOKEN_SQL);
    setSelectAccessTokenAuthenticationSql(SELECT_ACCESS_TOKEN_AUTHENTICATION_SQL);
    setSelectAccessTokenFromAuthenticationSql(SELECT_ACCESS_TOKEN_FROM_AUTHENTICATION_SQL);
    setSelectAccessTokensFromUserNameAndClientIdSql(
        SELECT_ACCESS_TOKENS_FROM_USER_NAME_AND_CLIENT_ID_SQL);
    setSelectAccessTokensFromUserNameSql(SELECT_ACCESS_TOKENS_FROM_USER_NAME_SQL);
    setSelectAccessTokensFromClientIdSql(SELECT_ACCESS_TOKENS_FROM_CLIENT_ID_SQL);
    setDeleteAccessTokenSql(DELETE_ACCESS_TOKEN_SQL);
    setInsertRefreshTokenSql(INSERT_REFRESH_TOKEN_SQL);
    setSelectRefreshTokenSql(SELECT_REFRESH_TOKEN_SQL);
    setSelectRefreshTokenAuthenticationSql(SELECT_REFRESH_TOKEN_AUTHENTICATION_SQL);
    setDeleteRefreshTokenSql(DELETE_REFRESH_TOKEN_SQL);
    setDeleteAccessTokenFromRefreshTokenSql(DELETE_ACCESS_TOKEN_FROM_REFRESH_TOKEN_SQL);
  }

}
