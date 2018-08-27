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

package org.openlmis.auth.migration;

import static java.util.Locale.ENGLISH;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = BaseMigrationIntegrationTest.TestConfig.class)
public abstract class BaseMigrationIntegrationTest {
  private static final String INITIAL_TARGET = "20170214123504310";
  private static final MigrationVersion INITIAL_MIGRATION = MigrationVersion
      .fromVersion(INITIAL_TARGET);

  private static final String SQL_INSERT = "INSERT INTO %s(%s) VALUES (%s)";

  private static final String SQL_SELECT = "SELECT * FROM %s";

  static final String TABLE_PASSWORD_RESET_TOKENS = "password_reset_tokens";
  static final String TABLE_AUTH_USERS = "auth_users";

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private Flyway flyway;

  private AtomicInteger instanceNumber = new AtomicInteger(0);

  @Test
  public void shouldMigrate() {
    jdbcTemplate.execute("SET SCHEMA 'auth';");
    flyway.clean();

    setFlywayTarget(getTargetBeforeTestMigration(), INITIAL_MIGRATION);
    flyway.migrate();

    insertDataBeforeMigration();

    setFlywayTarget(getTestMigrationTarget(), MigrationVersion.LATEST);
    flyway.migrate();

    verifyDataAfterMigration();
  }

  /**
   * Prepares and puts data into database before the test migrations will be executed. The
   * database will be in version set by {@link #getTargetBeforeTestMigration()}.
   */
  abstract void insertDataBeforeMigration();

  /**
   * Returns to which migration Flyway should migrate before test. If the method returns null as
   * a value, the database will contain a initial schema and bootstrap data.
   */
  abstract String getTargetBeforeTestMigration();

  /**
   * Returns a test migration. if the method returns null as a value, the database will be
   * migrated to the latest version.
   */
  abstract String getTestMigrationTarget();

  /**
   * Verifies that data after migration are correct.
   */
  abstract void verifyDataAfterMigration();

  int getNextInstanceNumber() {
    return this.instanceNumber.incrementAndGet();
  }

  List<Map<String, Object>> getRows(String table) {
    return executeSelectQuery(SQL_SELECT, table);
  }

  void save(String table, Map<String, Object> row) {
    String argList = row.keySet().stream().collect(Collectors.joining(","));
    String valueList = argList.replaceAll("[^,]+", "?");
    String sql = createSql(SQL_INSERT, table, argList, valueList);

    jdbcTemplate.update(sql, row.values().toArray());
  }

  private List<Map<String, Object>> executeSelectQuery(String template, String... params) {
    String sql = createSql(template, params);
    return jdbcTemplate.queryForList(sql);
  }

  private String createSql(String template, String... params) {
    return String.format(ENGLISH, template, (Object[]) params);
  }

  private void setFlywayTarget(String target, MigrationVersion defaultValue) {
    flyway.setTarget(defaultValue);
    Optional
        .ofNullable(target)
        .ifPresent(elem -> flyway.setTarget(MigrationVersion.fromVersion(elem)));
  }

  @Configuration
  @EnableConfigurationProperties
  @PropertySource(value = "classpath:application.properties")
  public static class TestConfig {
    // We don't need all beans that are created in Application class.

    /**
     * Creates Flyway instance for migration tests.
     */
    @Bean
    @ConfigurationProperties("flyway")
    public Flyway flyway() {
      Flyway flyway = new Flyway();
      flyway.setDataSource(dataSource());
      flyway.setSchemas("auth");

      return flyway;
    }

    /**
     * Creates data source for migration tests.
     */
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
      return new org.apache.tomcat.jdbc.pool.DataSource();
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
      return new JdbcTemplate(dataSource());
    }

  }

}
