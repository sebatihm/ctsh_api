package com.ctsh.ctsh_api.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;


import com.ctsh.ctsh_api.Dtos.EntryResponseDto;
import com.ctsh.ctsh_api.Dtos.Validation.EntryRequestDto;
import com.ctsh.ctsh_api.Exceptions.ResourceNotFoundException;
import com.ctsh.ctsh_api.Mappers.EntryMapper;
import com.ctsh.ctsh_api.Mappers.UserMapper;
import com.ctsh.ctsh_api.Models.Entry;
import com.ctsh.ctsh_api.Models.User;
import com.ctsh.ctsh_api.Models.Enum.Role;
import com.ctsh.ctsh_api.Repositories.EntryRepository;
import com.ctsh.ctsh_api.Repositories.UserRepository;


@ExtendWith(MockitoExtension.class)
public class EntryServiceTest {
  @Mock
  private EntryRepository entryRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private FileService fileService;

  @InjectMocks
  private EntryService entryService;

  @BeforeEach
  private void setUp() {
    UserMapper userMapper = new UserMapper(fileService);
    EntryMapper entryMapper = new EntryMapper(userMapper);

    entryService = new EntryService(entryRepository, userRepository, entryMapper);
  }

  private User createTestUser(String uuid, String email, String name, Boolean isAuthenticated) {
    User user = new User();
    user.setUuid(uuid);
    user.setEmail(email);
    user.setName(name);
    user.setPassword("test-password");
    user.setProfilePicture("profile.jpg");
    user.setRole(Role.USER);
    
    if (isAuthenticated) {
      Authentication authentication =
        new UsernamePasswordAuthenticationToken(
            email,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );

      SecurityContextHolder.getContext()
        .setAuthentication(authentication);
      
      when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    }
    return user;
  }

  private User createAdminUser() {
    User user = new User();
    user.setUuid("admin-uuid");
    user.setEmail("admin@example.com");
    user.setName("admin-user");
    user.setPassword("admin-password");
    user.setRole(Role.ADMIN);

    Authentication authentication =
        new UsernamePasswordAuthenticationToken(
            user.getEmail(),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );

      SecurityContextHolder.getContext()
        .setAuthentication(authentication);
      
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

    return user;
  }

  private Entry createTestEntry(String uuid, User user) {
    Entry entry = new Entry();
    entry.setUuid(uuid);
    entry.setUser(user);
    entry.setDate(LocalDate.of(1968, 10, 2));
    entry.setDescription("Test Entry");
    return entry;
  }

  @Test
  void testGetEntries() {
    User user = createTestUser("user-uuid", "user@example.com", "test-user", false);
    Entry entry = createTestEntry("entry-uuid", user);
    Entry entry2 = createTestEntry("entry-uuid2", user);

    when(entryRepository.findAll()).thenReturn(List.of(entry, entry2));

    List<EntryResponseDto> result = entryService.getEntries();

    assertEquals(2, result.size());
    assertEquals("entry-uuid", result.get(0).getUuid());
    assertEquals("entry-uuid2", result.get(1).getUuid());

  }

  @Test
  void testGetEntriesByUser() {
    User user = createTestUser("user-uuid", "user@example.com", "test-user", false);
    Entry entry = createTestEntry("entry-uuid", user);

    when(entryRepository.findByUser(user)).thenReturn(List.of(entry));
    when(userRepository.findById(user.getUuid())).thenReturn(Optional.of(user));

    List<EntryResponseDto> result = entryService.getEntriesByUserUuid("user-uuid");


    assertEquals(1, result.size());
    assertEquals("entry-uuid", result.get(0).getUuid());

  }



  @Test
  void testGetEntryById() {
    User user = createTestUser("user-uuid", "user@example.com", "test-user", false);
    Entry entry = createTestEntry("entry-uuid", user);

    when(entryRepository.findById(entry.getUuid())).thenReturn(Optional.of(entry));

    EntryResponseDto result = entryService.getEntryByUuid(entry.getUuid());

    assertEquals("entry-uuid", result.getUuid());

  }


  @Test
  void testCreateEntry() {
    User user = createTestUser("user-uuid", "user@example.com", "test-user", true);
    EntryRequestDto entry = new EntryRequestDto( LocalDate.of(1968, 10, 2), "Test Entry");

    when(fileService.getPublicUrl("profile.jpg")).thenReturn("profile.jpg");
    when(entryRepository.save(any(Entry.class))).thenAnswer(invocation -> invocation.getArgument(0));

    EntryResponseDto result = entryService.createEntry(entry);

    assertEquals("02/10/1968", result.getDate());
    assertEquals("Test Entry", result.getDescription());
    assertEquals(user.getUuid(), result.getUser().getUuid());

  }


  @Test
  void testCreateEntryWithNonExistentUser() {
    User user = createTestUser("user-uuid", "user@example.com", "test-user", false);

      Authentication authentication =
      new UsernamePasswordAuthenticationToken(
          user.getEmail(),
          null,
          List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
      );

      SecurityContextHolder.getContext()
        .setAuthentication(authentication);
      
      when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

    EntryRequestDto entry = new EntryRequestDto( LocalDate.of(1968, 10, 2), "Test Entry");


    assertThrows(
      ResourceNotFoundException.class,
      () -> entryService.createEntry(entry)
    );
  }


  @Test
  void testUpdateEntryByUser() {
    User user = createTestUser("user-uuid", "user@example.com", "test-user", true);
    Entry entry = createTestEntry("entry-uuid", user);

    when(entryRepository.findById(entry.getUuid())).thenReturn(Optional.of(entry));
    when(fileService.getPublicUrl("profile.jpg")).thenReturn("profile.jpg");

    when(entryRepository.save(any(Entry.class))).thenAnswer(invocation -> invocation.getArgument(0));
    EntryRequestDto entryDto = new EntryRequestDto( LocalDate.of(2001, 5, 11), null);

    EntryResponseDto result = entryService.updateEntry(entry.getUuid(),entryDto);

    assertEquals("11/05/2001", result.getDate());
    assertEquals(user.getUuid(), result.getUser().getUuid());

  }

  @Test
  void testUpdateEntryWithNonExistentEntry() {
    when(entryRepository.findById("non-existent-uuid")).thenReturn(Optional.empty());

    EntryRequestDto entryDto = new EntryRequestDto( LocalDate.of(2001, 5, 11), null);


    assertThrows(
      ResourceNotFoundException.class,
      () -> entryService.updateEntry("non-existent-uuid", entryDto)
    );

  }

  @Test
  void testUpdateEntryWithInvalidUser() {
    User user = createTestUser("user-uuid", "user@example.com", "test-user", false);
    createTestUser("user-uuid2", "user2@example.com", "test-user2", true);
    Entry entry = createTestEntry("entry-uuid", user);

    when(entryRepository.findById(entry.getUuid())).thenReturn(Optional.of(entry));

    assertThrows(
      AccessDeniedException.class,
      () -> entryService.updateEntry(entry.getUuid(), new EntryRequestDto( LocalDate.of(2001, 5, 11), null))
    );
  }

  @Test
  void testUpdateEntryWithAdmin() {
    User user = createTestUser("user-uuid", "user@example.com", "test-user", false);
    Entry entry = createTestEntry("entry-uuid", user);
    createAdminUser();

    when(entryRepository.findById(entry.getUuid())).thenReturn(Optional.of(entry));
    when(fileService.getPublicUrl("profile.jpg")).thenReturn("profile.jpg");

    when(entryRepository.save(any(Entry.class)))
    .thenAnswer(invocation -> invocation.getArgument(0));

    EntryResponseDto result = entryService.updateEntry("entry-uuid", new EntryRequestDto( LocalDate.of(2001, 5, 11), null));

    assertEquals("11/05/2001", result.getDate());
    assertEquals(user.getUuid(), result.getUser().getUuid());

  }

  @Test
  void testDeleteEntryByUser() {
    User user = createTestUser("user-uuid", "user@example.com", "test-user", true);
    Entry entry = createTestEntry("entry-uuid", user);

    when(entryRepository.findById(entry.getUuid())).thenReturn(Optional.of(entry));

    entryService.deleteEntry("entry-uuid");
    verify(entryRepository).delete(entry);
  }

  @Test
  void testDeleteEntryWithInvalidUser() {
    User user = createTestUser("user-uuid", "user@example.com", "test-user", false);
    createTestUser("user-uuid2", "user2@example.com", "test-user2", true);
    Entry entry = createTestEntry("entry-uuid", user);
    
    when(entryRepository.findById(entry.getUuid())).thenReturn(Optional.of(entry));

    assertThrows(
      AccessDeniedException.class,
      () -> entryService.deleteEntry(entry.getUuid())
    );
  }

  @Test
  void testDeleteEntryWithAdmin() {
    User user = createTestUser("user-uuid", "user@example.com", "test-user", false);
    Entry entry = createTestEntry("entry-uuid", user);
    createAdminUser();
    
    when(entryRepository.findById(entry.getUuid())).thenReturn(Optional.of(entry));

    entryService.deleteEntry("entry-uuid");
    verify(entryRepository).delete(entry);
  }

}
