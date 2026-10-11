package com.ctsh.ctsh_api.Dtos;

import lombok.Data;

@Data
public class EntryResponseDto {
  private String uuid;
  private String date;
  private UserResponseDto user;
  private String description;
}
