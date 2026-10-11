package com.ctsh.ctsh_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${app.uploads.dir}")
  private String uploadPath;

  @Value("${app.uploads.url-path}")
  private String uploadUrl;

  @Value ("${app.url.frontend}")
  private String frontendUrl;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler(uploadUrl + "/**")
        .addResourceLocations("file:" + uploadPath + "/");
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
      .allowedOrigins(
        frontendUrl
      )
      .allowedMethods(
        "GET",
        "POST",
        "PUT",
        "DELETE",
        "OPTIONS"
      )
      .allowedHeaders("*")
      .exposedHeaders("Authorization")
      .allowCredentials(true)
      .maxAge(3600);
  }
}
