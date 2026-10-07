package com.ctsh.ctsh_api.Mappers;

import org.springframework.stereotype.Component;

import com.ctsh.ctsh_api.Dtos.MoodResponseDto;
import com.ctsh.ctsh_api.Models.Mood;

@Component 
public class MoodMapper {
  public MoodResponseDto toResponseDto(Mood mood) {
    MoodResponseDto dto = new MoodResponseDto();
    dto.setName(mood.getName());
    dto.setCount(mood.getCount());
    return dto;
  }
}
