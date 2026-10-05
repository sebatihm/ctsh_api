package com.ctsh.ctsh_api.Controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.ctsh.ctsh_api.Services.CookieService;
import com.ctsh.ctsh_api.Services.JwtService;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {
  @Autowired
  private MockMvc mockMvc;
  
  @MockitoBean
  private AuthenticationProvider authenticationManager;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private CookieService cookieService;

  @Test
  void testLogin() throws Exception {
    when(jwtService.generateToken("john.doe@example.com"))
    .thenReturn("fake-jwt-token");

     String body = """
        {
            "email": "john.doe@example.com",
            "password": "password123"
        }
        """;

    mockMvc.perform(
        post("/login")
            .content(body)
            .contentType(MediaType.APPLICATION_JSON)
    )
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.message").value("Login successful"));

    verify(authenticationManager).authenticate(any());

    verify(jwtService)
        .generateToken("john.doe@example.com");

    verify(cookieService)
        .setCookie(any(), org.mockito.ArgumentMatchers.eq("fake-jwt-token"));
  }

    @Test
    void testLoginWithoutEmail() throws Exception {
     String body = """
        {
            "password": "password123"
        }
        """;

    mockMvc.perform(
        post("/login")
            .content(body)
            .contentType(MediaType.APPLICATION_JSON)
    )
    .andExpect(status().isBadRequest());
  }

  @Test
  void testLoginWithInvalidEmail() throws Exception {

     String body = """
        {
            "email": "invalid-email",
            "password": "password123"
        }
        """;

    mockMvc.perform(
        post("/login")
            .content(body)
            .contentType(MediaType.APPLICATION_JSON)
    )
    .andExpect(status().isBadRequest());
  }

  @Test
  void testLoginWithInvalidCredentials() throws Exception {
    when(authenticationManager.authenticate(any()))
    .thenThrow(new BadCredentialsException("Bad credentials"));

     String body = """
        {
            "email": "john.doe@example.com",
            "password": "password123"
        }
        """;

    mockMvc.perform(
        post("/login")
            .content(body)
            .contentType(MediaType.APPLICATION_JSON)
    )
    .andExpect(status().isUnauthorized())
    .andExpect(jsonPath("$.message").value("Invalid email or password"));

    verify(authenticationManager).authenticate(any());

  }


  @Test
  void testLogout() throws Exception {

      mockMvc.perform(
          post("/logout")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.message").value("Logout successful"));

      verify(cookieService).deleteCookie(any());
  }
  
}
