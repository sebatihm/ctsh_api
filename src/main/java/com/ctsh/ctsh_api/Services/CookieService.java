package com.ctsh.ctsh_api.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service 
public class CookieService {

  @Value("${app.cookie.name}")
  private String cookieName;

  @Value("${app.cookie.max-age-seconds}")
  private int cookieMaxAgeSeconds;

  @Value ("${app.cookie.secure}")
  private boolean cookieSecure;

  @Value ("${app.cookie.same-site}")
  private String cookieSameSite;
  
  public void setCookie(HttpServletResponse response, String cookieValue) {
    ResponseCookie cookie = ResponseCookie.from(cookieName, cookieValue)
        .httpOnly(true)
        .secure(cookieSecure)
        .path("/")
        .maxAge(cookieMaxAgeSeconds)
        .sameSite(cookieSameSite)
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  public String getCookieValue(HttpServletRequest request) {
      Cookie cookie = WebUtils.getCookie(request, cookieName);
      return cookie != null ? cookie.getValue() : null;
  }

  public void deleteCookie(HttpServletResponse response) {
    ResponseCookie cookie = ResponseCookie.from(cookieName, "")
        .httpOnly(true)
        .secure(cookieSecure)
        .path("/")
        .maxAge(0)
        .sameSite(cookieSameSite)
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}
