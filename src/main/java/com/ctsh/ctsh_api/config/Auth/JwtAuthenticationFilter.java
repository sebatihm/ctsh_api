package com.ctsh.ctsh_api.config.Auth;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ctsh.ctsh_api.Services.CookieService;
import com.ctsh.ctsh_api.Services.JwtService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  private final CookieService cookieService;

  public JwtAuthenticationFilter(JwtService jwtService, CookieService cookieService) {
    this.jwtService = jwtService;
    this.cookieService = cookieService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
      String header = request.getHeader("Authorization");
      boolean tokenFromCookie = false;
      String token;

      if (header != null && header.startsWith("Bearer ")) {
        token = header.substring(7);
      } else {
        token = cookieService.getCookieValue(request);
        tokenFromCookie = token != null;
      }

      if (token == null || token.isBlank()) {
        filterChain.doFilter(request, response);
        return;
      }

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
      if (tokenFromCookie) {
        cookieService.deleteCookie(response);
      }
    } catch (JwtException | IllegalArgumentException e) {
      request.setAttribute("jwt_error", "Invalid token or signature");
      SecurityContextHolder.clearContext();
      if (tokenFromCookie) {
        cookieService.deleteCookie(response);
      }
    }


    filterChain.doFilter(request, response);
  }


  }