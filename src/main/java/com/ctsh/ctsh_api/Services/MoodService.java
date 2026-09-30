package com.ctsh.ctsh_api.Services;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.ctsh.ctsh_api.Dtos.MoodResponseDto;
import com.ctsh.ctsh_api.Exceptions.BadRequestException;
import com.ctsh.ctsh_api.Exceptions.ResourceNotFoundException;
import com.ctsh.ctsh_api.Models.Mood;
import com.ctsh.ctsh_api.Repositories.MoodRepository;

@Service
public class MoodService {

  private static final Pattern VALID_NAME = Pattern.compile("^[a-z0-9_-]{1,50}$");

  private final MoodRepository moodRepository;

  public MoodService(MoodRepository moodRepository) {
    this.moodRepository = moodRepository;
  }

  public List<MoodResponseDto> getAllMoods() {
    return moodRepository.findAll().stream()
        .map(this::toResponseDto)
        .toList();
  }

  public MoodResponseDto getMoodByName(String name) {
    String normalizedName = normalizeName(name);
    return toResponseDto(findMoodByName(normalizedName));
  }

  public MoodResponseDto incrementMoodCount(String name) {
    String normalizedName = normalizeName(name);
    moodRepository.upsertIncrement(normalizedName);
    return getMoodByName(normalizedName);
  }

  public void deleteMoodByName(String name) {
    String normalizedName = normalizeName(name);
    Mood mood = findMoodByName(normalizedName);
    moodRepository.delete(mood);
  }

  private Mood findMoodByName(String name) {
    Mood mood = moodRepository.findByName(name)
        .orElseThrow(() -> new ResourceNotFoundException("Mood not found with name: " + name));
    return mood;
  }

  private String normalizeName(String name) {
    String normalized = name.trim().toLowerCase(Locale.ROOT);
    if (!VALID_NAME.matcher(normalized).matches()) {
      throw new BadRequestException("Invalid mood name: must be 1-50 characters without spaces and only contain lowercase letters, numbers, underscores, or hyphens.");
    }
    return normalized;
  }

  private MoodResponseDto toResponseDto(Mood mood) {
    MoodResponseDto dto = new MoodResponseDto();
    dto.setName(mood.getName());
    dto.setCount(mood.getCount());
    return dto;
  }
}
