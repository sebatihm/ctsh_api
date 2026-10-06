package com.ctsh.ctsh_api.Dtos;

import lombok.Data;

@Data 
public class MailResponseDto {
  String uuid;
  UserResponseDto fromUuuid;
  UserResponseDto toUuuid;
  String text;
}
