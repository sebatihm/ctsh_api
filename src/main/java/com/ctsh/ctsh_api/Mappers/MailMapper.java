package com.ctsh.ctsh_api.Mappers;

import org.springframework.stereotype.Component;

import com.ctsh.ctsh_api.Dtos.MailResponseDto;
import com.ctsh.ctsh_api.Dtos.UserResponseDto;
import com.ctsh.ctsh_api.Models.Mail;

@Component 
public class MailMapper {

  private final UserMapper userMapper;

  public MailMapper(UserMapper userMapper) {
    this.userMapper = userMapper;
  }

  public MailResponseDto toResponseDto(Mail mail) {
    UserResponseDto fromUserDto = userMapper.toResponseDto(mail.getFrom());
    UserResponseDto toUserDto = userMapper.toResponseDto(mail.getTo());

    MailResponseDto dto = new MailResponseDto();
    dto.setUuid(mail.getUuid());
    dto.setFrom(fromUserDto);
    dto.setTo(toUserDto);
    dto.setMessage(mail.getMessage());
    return dto;
  }
  
}
