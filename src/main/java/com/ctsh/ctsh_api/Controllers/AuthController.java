package com.ctsh.ctsh_api.Controllers;

import org.springframework.web.bind.annotation.RestController;

import com.ctsh.ctsh_api.Dtos.LoginDto;
import com.ctsh.ctsh_api.Services.JwtService;

import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
public class AuthController {

  @Autowired 
  private AuthenticationProvider authenticationManager;

  @Autowired
  private JwtService jwtService;

  
  public AuthController(AuthenticationProvider authenticationManager, JwtService jwtService) {
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
  }

  @PostMapping("login")
  public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginDto loginDto) {
    try {
      this.authenticationManager.authenticate(
        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
          loginDto.getEmail(), loginDto.getPassword()
        )
      );

      return ResponseEntity.ok(Map.of("token", jwtService.generateToken(loginDto.getEmail())));

    } catch (org.springframework.security.core.AuthenticationException e) {
      return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
    }
  }
  
}
