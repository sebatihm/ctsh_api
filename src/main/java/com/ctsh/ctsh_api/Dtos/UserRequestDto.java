package com.ctsh.ctsh_api.Dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequestDto {
  @NotNull(message = "Name cannot be null")
  @NotBlank(message = "Name cannot be blank")
  private String name;

  @NotNull(message = "Email cannot be null")
  @NotBlank(message = "Email cannot be blank")
  @Email(message = "Email should be valid")
  private String email;

  @NotNull(message = "Password cannot be null")
  @NotBlank(message = "Password cannot be blank")
  @Size(min = 8, message = "Password must be at least 8 characters long")
  private String password;
}