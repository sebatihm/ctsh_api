package com.ctsh.ctsh_api.Dtos;

import lombok.Data;

@Data
public class EntryResponseDto {
  private String uuid;
  private String date;
  private UserResponseDto userUuid;
  private String description;
}
