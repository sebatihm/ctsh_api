package com.ctsh.ctsh_api.Services;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.ctsh.ctsh_api.Dtos.MoodResponseDto;
import com.ctsh.ctsh_api.Exceptions.BadRequestException;
import com.ctsh.ctsh_api.Exceptions.ResourceNotFoundException;
import com.ctsh.ctsh_api.Mappers.MoodMapper;
import com.ctsh.ctsh_api.Models.Mood;
import com.ctsh.ctsh_api.Repositories.MoodRepository;

@Service
public class MoodService {

  private static final Pattern VALID_NAME = Pattern.compile("^[a-z0-9_-]{1,50}$");

  private final MoodRepository moodRepository;
  private final MoodMapper moodMapper;

  public MoodService(MoodRepository moodRepository, MoodMapper moodMapper) {
    this.moodRepository = moodRepository;
    this.moodMapper = moodMapper;
  }

  public List<MoodResponseDto> getAllMoods() {
    return moodRepository.findAll().stream()
        .map(moodMapper::toResponseDto)
        .toList();
  }

  public MoodResponseDto getMoodByName(String name) {
    String normalizedName = normalizeName(name);
    return moodMapper.toResponseDto(findMoodByName(normalizedName));
  }

  public MoodResponseDto incrementMoodCount(String name) {
    String normalizedName = normalizeName(name);
    moodRepository.upsertIncrement(normalizedName);
    return moodMapper.toResponseDto(findMoodByName(normalizedName));
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
      throw new BadRequestException("Invalid mood name: must be 1-50 characters, letters, numbers, underscores, or hyphens.");
    }
    return normalized;
  }

}
