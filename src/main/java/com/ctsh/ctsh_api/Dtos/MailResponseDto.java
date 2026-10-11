package com.ctsh.ctsh_api.Dtos;

import lombok.Data;

@Data 
public class MailResponseDto {
  String uuid;
  UserResponseDto from;
  UserResponseDto to;
  String message;
}
