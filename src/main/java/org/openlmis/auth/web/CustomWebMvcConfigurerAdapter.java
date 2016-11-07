package org.openlmis.auth.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class CustomWebMvcConfigurerAdapter extends WebMvcConfigurerAdapter {

  @Value("${server.url}")
  private String serverUrl;

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/docs").setViewName("redirect:" + serverUrl + "/docs/");
    registry.addViewController("/docs/").setViewName("forward:/docs/index.html");
    super.addViewControllers(registry);
  }
}
