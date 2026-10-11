package com.ctsh.ctsh_api.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ctsh.ctsh_api.Dtos.MoodResponseDto;
import com.ctsh.ctsh_api.Exceptions.BadRequestException;
import com.ctsh.ctsh_api.Exceptions.ResourceNotFoundException;
import com.ctsh.ctsh_api.Mappers.MoodMapper;
import com.ctsh.ctsh_api.Models.Mood;
import com.ctsh.ctsh_api.Repositories.MoodRepository;

@ExtendWith(MockitoExtension.class)
public class MoodServiceTest {

  @Mock 
  private MoodRepository moodRepository;

  private MoodMapper moodMapper;

  @InjectMocks
  private MoodService moodService;

  private Mood createTestMood(String name, int count) {
    Mood mood = new Mood();
    mood.setName(name);
    mood.setCount(count);
    return mood;
  }

  @BeforeEach
  void setUp() {
    moodMapper = new MoodMapper();
    moodService = new MoodService(
        moodRepository,
        moodMapper
    );
  }
  
  @Test 
  void testGetAllMoods() {
    when(moodRepository.findAll()).thenReturn(List.of(createTestMood("Happy", 10), createTestMood("Sad", 5)));
    List<MoodResponseDto> result = moodService.getAllMoods();
    assertEquals(2, result.size());
    assertEquals("Happy", result.get(0).getName());
    assertEquals(10, result.get(0).getCount());
    assertEquals("Sad", result.get(1).getName());
    assertEquals(5, result.get(1).getCount());
  }

  @Test
  void testGetMoodByName() {
    when(moodRepository.findByName("happy")).thenReturn(java.util.Optional.of(createTestMood("happy", 10)));
    MoodResponseDto result = moodService.getMoodByName("Happy");
    assertEquals("happy", result.getName());
    assertEquals(10, result.getCount());
  }

  @Test
  void testGetMoodByNameNotFound() {
    when(moodRepository.findByName("happy")).thenReturn(java.util.Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> {
      moodService.getMoodByName("Happy");
    });
  }

  @Test
  void testIncrementMoodCountWithInvalidName() {
    assertThrows(BadRequestException.class, () -> {
      moodService.incrementMoodCount("");
    });
  }

  @Test
  void testIncrementMoodCountWithInvalidCharacters() {
    assertThrows(BadRequestException.class, () -> {
      moodService.incrementMoodCount("no/slash");
    });
  }

  @Test
  void testIncrementMoodCount() {
    when(moodRepository.findByName("happy")).thenReturn(java.util.Optional.of(createTestMood("happy", 10)));
    
    MoodResponseDto result = moodService.incrementMoodCount("Happy");
    assertEquals("happy", result.getName());
    verify(moodRepository)
      .upsertIncrement("happy");
  }

  @Test
  void testIncrementMoodCountAcceptsSpacesAndAccents() {
    when(moodRepository.findByName("muy felíz")).thenReturn(java.util.Optional.of(createTestMood("muy felíz", 3)));

    MoodResponseDto result = moodService.incrementMoodCount("  Muy Felíz  ");

    assertEquals("muy felíz", result.getName());
    verify(moodRepository).upsertIncrement("muy felíz");
  }

  @Test
  void testIncrementMoodCountKeepsInternalSpaces() {
    when(moodRepository.findByName("very  happy")).thenReturn(java.util.Optional.of(createTestMood("very  happy", 1)));

    MoodResponseDto result = moodService.incrementMoodCount("Very  Happy");

    assertEquals("very  happy", result.getName());
    verify(moodRepository).upsertIncrement("very  happy");
  }

  @Test
  void testNormalizeNameAppliesUnicodeNfc() {
    when(moodRepository.findByName("café")).thenReturn(java.util.Optional.of(createTestMood("café", 1)));

    MoodResponseDto result = moodService.getMoodByName("Cafe\u0301");

    assertEquals("café", result.getName());
  }

  @Test
  void testGetMoodByNameWithSpaces() {
    when(moodRepository.findByName("very happy")).thenReturn(java.util.Optional.of(createTestMood("very happy", 7)));

    MoodResponseDto result = moodService.getMoodByName("Very Happy");

    assertEquals("very happy", result.getName());
    assertEquals(7, result.getCount());
  }
}
