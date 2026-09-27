package com.ctsh.ctsh_api.Controllers;

import org.springframework.web.bind.annotation.RestController;

import com.ctsh.ctsh_api.Dtos.ApiResponse;
import com.ctsh.ctsh_api.Dtos.Validation.LoginDto;
import com.ctsh.ctsh_api.Exceptions.UnauthorizedException;
import com.ctsh.ctsh_api.Services.CookieService;
import com.ctsh.ctsh_api.Services.JwtService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
public class AuthController {

  private final AuthenticationProvider authenticationManager;

  private final JwtService jwtService;

  private final CookieService cookieService;

  
  public AuthController(AuthenticationProvider authenticationManager, JwtService jwtService, CookieService cookieService) {
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.cookieService = cookieService;
  }

  @PostMapping("login")
  public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginDto loginDto, HttpServletResponse response) {
    try {
      this.authenticationManager.authenticate(
        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
          loginDto.email(), loginDto.password()
        )
      );

      this.cookieService.setCookie(response, jwtService.generateToken(loginDto.email()));

      return ResponseEntity.status(HttpStatus.OK).body(
        ApiResponse.of(HttpStatus.OK, "Login successful", null)
      );

    } catch (org.springframework.security.core.AuthenticationException e) {
      throw new UnauthorizedException("Invalid email or password");
    }
  }

}
