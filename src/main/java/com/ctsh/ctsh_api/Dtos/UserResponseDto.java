package com.ctsh.ctsh_api.Dtos;

import com.ctsh.ctsh_api.Models.Enum.Role;

import lombok.Data;

@Data
public class UserResponseDto {
  private String uuid;
  private String name;
  private String email;
  private Role role;
  private String profilePicture;
}