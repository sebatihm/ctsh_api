package com.ctsh.ctsh_api.Repositories;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import com.ctsh.ctsh_api.Models.Mood;

@DataJpaTest 
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class MoodRepositoryTest {
  @Autowired
  private MoodRepository moodRepository;

  @Test 
  void testFindByNameDoesNotReturnMoodIfNotExists() {
    Mood foundMood = moodRepository.findByName("Happy").orElse(null);
    assertNull(foundMood);
  }
  
  @Test 
  void testFindByNameReturnsMood() {
    Mood mood = new Mood();
    mood.setName("Happy");
    mood.setCount(1);
    moodRepository.save(mood);

    Mood foundMood = moodRepository.findByName("Happy").orElse(null);
    assertNotNull(foundMood);
  }

  @Test
  void testupsertIncrementInsertsNewMood() {
    moodRepository.upsertIncrement("Excited");
    Mood foundMood = moodRepository.findByName("Excited").orElse(null);
    assertNotNull(foundMood);
    assert(foundMood.getCount() == 1);
  }

  @Test
  void testupsertIncrementIncrementsExistingMood() {
    Mood mood = new Mood();
    mood.setName("Excited");
    mood.setCount(1);
    moodRepository.saveAndFlush(mood);

    moodRepository.upsertIncrement("Excited");
    Mood foundMood = moodRepository.findByName("Excited").orElse(null);
    assertNotNull(foundMood);
    assert(foundMood.getCount() == 2);
  }

  @Test
  void testSaveRejectsDuplicateMood() {
    Mood mood1 = new Mood();
    mood1.setName("Excited");
    mood1.setCount(1);
    moodRepository.saveAndFlush(mood1);

    Mood mood2 = new Mood();
    mood2.setName("Excited");
    mood2.setCount(1);
    
    assertThrows(DataIntegrityViolationException.class, () -> {
      moodRepository.saveAndFlush(mood2);
    });
  
  }

  @Test
  void testSaveRejectsMoodWithNullName() {
    Mood mood = new Mood();
    mood.setName(null);
    mood.setCount(1);
    
    assertThrows(DataIntegrityViolationException.class, () -> {
      moodRepository.saveAndFlush(mood);
    });
  }

  @Test
  void testDeleteRemovesMood() {
    Mood mood = new Mood();
    mood.setName("Excited");
    mood.setCount(1);
    moodRepository.saveAndFlush(mood);

    Mood foundMood = moodRepository.findByName("Excited").orElse(null);
    assertNotNull(foundMood);

    moodRepository.delete(foundMood);
    Mood deletedMood = moodRepository.findByName("Excited").orElse(null);
    assertNull(deletedMood);
  }
}
