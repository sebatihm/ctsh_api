package com.ctsh.ctsh_api.Controllers;

import org.springframework.web.bind.annotation.RestController;

import com.ctsh.ctsh_api.Dtos.LoginDto;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
public class AuthController {
  @PostMapping("login")
  public String login(@Valid @RequestBody LoginDto loginDto) {
    return "Login successful for user: " + loginDto.getEmail();
  }
  
}
