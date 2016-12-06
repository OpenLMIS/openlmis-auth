package org.openlmis.auth.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class CustomWebMvcConfigurerAdapter extends WebMvcConfigurerAdapter {

  @Value("${service.url}")
  private String serviceUrl;

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/auth/docs").setViewName("redirect:" + serviceUrl + "/auth/docs/");
    registry.addViewController("/auth/docs/").setViewName("forward:/auth/docs/index.html");
    super.addViewControllers(registry);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/auth/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
    super.addResourceHandlers(registry);
  }
}
