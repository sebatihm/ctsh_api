package com.ctsh.ctsh_api.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class CookieServiceTest {
  @Mock
  private HttpServletResponse response;

  @Mock
  private HttpServletRequest request;

  private CookieService cookieService;

  @BeforeEach
  void setUp() {

    cookieService = new CookieService();

    ReflectionTestUtils.setField(cookieService, "cookieName", "jwt");
    ReflectionTestUtils.setField(cookieService, "cookieMaxAgeSeconds", 3600);
    ReflectionTestUtils.setField(cookieService, "cookieSecure", true);
    ReflectionTestUtils.setField(cookieService, "cookieSameSite", "Strict");
  }

  @Test
  void testSetCookie() {

      cookieService.setCookie(response, "my-jwt-token");

      ArgumentCaptor<String> captor =
          ArgumentCaptor.forClass(String.class);

      verify(response).addHeader(
          eq(HttpHeaders.SET_COOKIE),
          captor.capture()
      );

      String header = captor.getValue();

      assertTrue(header.contains("jwt=my-jwt-token"));
      assertTrue(header.contains("HttpOnly"));
      assertTrue(header.contains("Secure"));
      assertTrue(header.contains("Path=/"));
      assertTrue(header.contains("Max-Age=3600"));
      assertTrue(header.contains("SameSite=Strict"));
  }

  @Test
  void testGetCookieValue() {
    
    Cookie cookie = new Cookie("jwt", "my-jwt-token");

    when(request.getCookies())
        .thenReturn(new Cookie[] { cookie });

    String result = cookieService.getCookieValue(request);

    assertEquals("my-jwt-token", result);
  }

  @Test
  void testReturnNullWhenCookieDoesNotExist() {

      when(request.getCookies())
          .thenReturn(null);

      String result =
          cookieService.getCookieValue(request);

      assertNull(result);
  }

  @Test
  void testGetJwtCookieAmongMultipleCookies() {

      Cookie session = new Cookie("session", "abc");
      Cookie jwt = new Cookie("jwt", "my-jwt-token");

      when(request.getCookies())
          .thenReturn(new Cookie[] {
              session,
              jwt
          });

      String result =
          cookieService.getCookieValue(request);

      assertEquals("my-jwt-token", result);
  }

  @Test
  void testDeleteCookie() {

    cookieService.deleteCookie(response);

    ArgumentCaptor<String> captor =
        ArgumentCaptor.forClass(String.class);

    verify(response).addHeader(
        eq(HttpHeaders.SET_COOKIE),
        captor.capture()
    );

    String header = captor.getValue();

    assertTrue(header.contains("jwt=;"));
    assertTrue(header.contains("HttpOnly"));
    assertTrue(header.contains("Secure"));
    assertTrue(header.contains("Path=/"));
    assertTrue(header.contains("Max-Age=0"));
    assertTrue(header.contains("SameSite=Strict"));
  }


}
