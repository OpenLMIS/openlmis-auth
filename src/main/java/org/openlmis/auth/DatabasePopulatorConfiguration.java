package org.openlmis.auth;

import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration
public class DatabasePopulatorConfiguration {

  @Autowired
  private PGPoolingDataSource dataSource;

  /**
   * Initializes database using SQL script.
   * @return database populator
   */
  @Bean
  public ResourceDatabasePopulator databasePopulator() {
    ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
    populator.setSqlScriptEncoding("UTF-8");
    populator.addScript(new ClassPathResource("initial_data.sql"));
    return populator;
  }

  @Bean
  public InitializingBean populatorExecutor() {
    return () -> DatabasePopulatorUtils.execute(databasePopulator(), dataSource);
  }

}
