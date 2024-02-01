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

package org.openlmis.auth;

import java.io.IOException;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("demo-data & !test-run")
@Order(5)
public class TestDataInitializer implements CommandLineRunner {

  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(TestDataInitializer.class);

  private static final String DEMO_DATA_PATH = "classpath:db/demo-data/";
  private static final String FILE_EXTENSION = ".csv";

  // table names
  private static final String API_KEYS = "api_keys";
  private static final String AUTH_USERS = "auth_users";
  private static final String OAUTH_CLIENT_DETAILS = "oauth_client_details";

  // database path
  private static final String DB_SCHEMA = "auth.";
  static final String API_KEYS_TABLE = DB_SCHEMA + API_KEYS;
  static final String AUTH_USERS_TABLE = DB_SCHEMA + AUTH_USERS;
  static final String OAUTH_CLIENT_DETAILS_TABLE = DB_SCHEMA + OAUTH_CLIENT_DETAILS;


  @Value(value = DEMO_DATA_PATH + DB_SCHEMA + API_KEYS + FILE_EXTENSION)
  private Resource apiKeysResource;

  @Value(value = DEMO_DATA_PATH + DB_SCHEMA + AUTH_USERS + FILE_EXTENSION)
  private Resource authUsersResource;

  @Value(value = DEMO_DATA_PATH + DB_SCHEMA + OAUTH_CLIENT_DETAILS + FILE_EXTENSION)
  private Resource oauthClientDetailsResource;

  private Resource2Db loader;

  @Autowired
  public TestDataInitializer(JdbcTemplate template) {
    this(new Resource2Db(template));
  }

  TestDataInitializer(Resource2Db loader) {
    this.loader = loader;
  }

  /**
   * Initializes test data.
   *
   * @param args command line arguments
   */
  public void run(String... args) throws IOException {
    XLOGGER.entry();

    loader.insertToDbFromCsv(AUTH_USERS_TABLE, authUsersResource);
    loader.insertToDbFromCsv(OAUTH_CLIENT_DETAILS_TABLE, oauthClientDetailsResource);
    loader.insertToDbFromCsv(API_KEYS_TABLE, apiKeysResource);

    XLOGGER.exit();
  }

}
