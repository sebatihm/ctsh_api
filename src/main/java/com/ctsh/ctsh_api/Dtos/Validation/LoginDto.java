package com.ctsh.ctsh_api.Dtos.Validation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginDto (

  @NotBlank(message = "Email cannot be blank")
  @Email(message = "Email should be valid")
  String email,

  @NotNull(message = "Password cannot be null")
  @NotBlank(message = "Password cannot be blank")
  String password
){}
