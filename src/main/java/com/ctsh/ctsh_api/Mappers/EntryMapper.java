package com.ctsh.ctsh_api.Mappers;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.ctsh.ctsh_api.Dtos.EntryResponseDto;
import com.ctsh.ctsh_api.Models.Entry;

@Component
public class EntryMapper {

  private UserMapper userMapper;
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  public EntryMapper(UserMapper userMapper) {
    this.userMapper = userMapper;
  }

  public EntryResponseDto toResponseDto(Entry entry) {
    EntryResponseDto dto = new EntryResponseDto();

    dto.setUuid(entry.getUuid());
    dto.setDate(entry.getDate().format(formatter));
    dto.setUserUuid(userMapper.toResponseDto(entry.getUser()));
    dto.setDescription(entry.getDescription());

    return dto;
  }

  
}
