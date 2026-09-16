package com.ctsh.ctsh_api.Dtos;

import lombok.Data;

@Data
public class UserRequestDto {
  private String name;
  private String email;
  private String password;
  private String profilePicture;
}