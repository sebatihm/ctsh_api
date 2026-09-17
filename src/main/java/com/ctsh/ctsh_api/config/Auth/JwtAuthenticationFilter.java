package com.ctsh.ctsh_api.config.Auth;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ctsh.ctsh_api.Services.JwtService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  @Autowired
  private JwtService jwtService;

  public JwtAuthenticationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
      String header = request.getHeader("Authorization");

      if (header == null || !header.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
      }

      String token = header.substring(7);

    try {
      UserInfo userInfo = jwtService.decodeToken(token);
      String subject = userInfo.getEmail();
      String role = userInfo.getRole();

      if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        var auth = new UsernamePasswordAuthenticationToken(subject, null, authorities);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
      }
    } catch (ExpiredJwtException e) {
      request.setAttribute("jwt_error", "Expired token");
      SecurityContextHolder.clearContext();
    } catch (JwtException | IllegalArgumentException e) {
      request.setAttribute("jwt_error", "Invalid token or signature");
      SecurityContextHolder.clearContext();
    }


    filterChain.doFilter(request, response);
  }


  }