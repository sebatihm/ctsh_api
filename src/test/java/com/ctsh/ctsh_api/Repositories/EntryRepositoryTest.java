package com.ctsh.ctsh_api.Repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import com.ctsh.ctsh_api.Models.Entry;
import com.ctsh.ctsh_api.Models.User;

@DataJpaTest 
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EntryRepositoryTest {

  @Autowired
  private EntryRepository entryRepository;

  @Autowired
  private UserRepository userRepository;

  private User createTestEntryAndUser() {
    User user = new User();
    user.setName("John Doe");
    user.setEmail("john.doe@example.com");
    user.setProfilePicture("test.jpg");
    user.setPassword("password");
    user.setRole(com.ctsh.ctsh_api.Models.Enum.Role.USER);
    userRepository.save(user);
    
    return user;
  }

  private Entry createTestEntry(User user) {
    Entry entry = new Entry();
    entry.setUser(user);
    entry.setDescription("Test 1");
    entry.setDate(java.time.LocalDate.now());
    entryRepository.save(entry);
    
    return entry;
  }

  @Test
  void testSaveEntry() {

    User user = createTestEntryAndUser();
    createTestEntry(user);

    Entry result = entryRepository.findAll().get(0);
    assertTrue(result != null);
    assertEquals("Test 1", result.getDescription());
    assertEquals("John Doe", result.getUser().getName());
  }

  @Test
  void testFindEntryByUuid() {
    User user = createTestEntryAndUser();
    Entry entry = createTestEntry(user);

    Entry result = entryRepository.findById(entry.getUuid()).orElse(null);
    assertTrue(result != null);
    assertEquals("Test 1", result.getDescription());
    assertEquals("John Doe", result.getUser().getName());
  }

  @Test
  void testFindEntriesByUser() {
    User user = createTestEntryAndUser();
    createTestEntry(user);

    List<Entry> results = entryRepository.findByUser(user);
    assertTrue(!results.isEmpty());
    assertEquals("Test 1", results.get(0).getDescription());
    assertEquals("John Doe", results.get(0).getUser().getName());
  }

  @Test
  void testFindEntries() {
    User user = createTestEntryAndUser();
    createTestEntry(user);

    List<Entry> results = entryRepository.findAll();
    assertTrue(!results.isEmpty());
    assertEquals("Test 1", results.get(0).getDescription());
    assertEquals("John Doe", results.get(0).getUser().getName());
  }

  @Test
  void testUpdateEntry() {
    User user = createTestEntryAndUser();
    Entry entry = createTestEntry(user);

    entry.setDescription("Updated Test 1");
    entryRepository.save(entry);

    Entry result = entryRepository.findById(entry.getUuid()).orElse(null);
    assertTrue(result != null);
    assertEquals("Updated Test 1", result.getDescription());
  }

  @Test
  void testDeleteEntry() {
    User user = createTestEntryAndUser();
    Entry entry = createTestEntry(user);

    entryRepository.delete(entry);

    Entry result = entryRepository.findById(entry.getUuid()).orElse(null);
    assertTrue(result == null);
  }

}