package com.ctsh.ctsh_api.Dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class LoginDto {

  @Getter @Setter 
  @NotBlank(message = "Email cannot be blank")
  @Email(message = "Email should be valid")
  private String email;

  @Getter @Setter 
  @NotNull(message = "Password cannot be null")
  @NotBlank(message = "Password cannot be blank")
  private String password;
}
