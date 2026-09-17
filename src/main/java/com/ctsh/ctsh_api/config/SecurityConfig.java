package com.ctsh.ctsh_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ctsh.ctsh_api.config.Auth.CustomAccessDeniedHandler;
import com.ctsh.ctsh_api.config.Auth.CustomAuthenticationEntryPoint;
import com.ctsh.ctsh_api.config.Auth.JwtAuthenticationFilter;
import com.ctsh.ctsh_api.config.Auth.PepperedPasswordEncoder;

@Configuration 
@EnableWebSecurity 
public class SecurityConfig {
  
  
  @Value("${app.security.pepper}")
  private String pepper;
  
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final CustomAccessDeniedHandler accessDeniedHandler;
  private final CustomAuthenticationEntryPoint entryPoint;

  SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                 CustomAccessDeniedHandler accessDeniedHandler,
                 CustomAuthenticationEntryPoint entryPoint) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.accessDeniedHandler = accessDeniedHandler;
    this.entryPoint = entryPoint;
  }

  @Bean 
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
         .exceptionHandling(ex -> ex
            .authenticationEntryPoint(entryPoint)
            .accessDeniedHandler(accessDeniedHandler))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST, "/user").permitAll()
            .requestMatchers("/user/**").hasRole("ADMIN")
            .requestMatchers("/login").permitAll()
            .requestMatchers("/error").permitAll()
            .anyRequest().authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new PepperedPasswordEncoder(pepper);
  }

  @Bean
  public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
    var provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
  }

}
