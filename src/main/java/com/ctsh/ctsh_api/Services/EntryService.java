package com.ctsh.ctsh_api.Services;


import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ctsh.ctsh_api.Dtos.EntryResponseDto;
import com.ctsh.ctsh_api.Dtos.Validation.EntryRequestDto;
import com.ctsh.ctsh_api.Exceptions.ResourceNotFoundException;
import com.ctsh.ctsh_api.Mappers.EntryMapper;
import com.ctsh.ctsh_api.Models.Entry;
import com.ctsh.ctsh_api.Models.User;
import com.ctsh.ctsh_api.Models.Enum.Role;
import com.ctsh.ctsh_api.Repositories.EntryRepository;
import com.ctsh.ctsh_api.Repositories.UserRepository;

@Service
public class EntryService {
  
  private final EntryRepository entryRepository;
  private final UserRepository userRepository;
  private final EntryMapper entryMapper;

  public EntryService(EntryRepository entryRepository, UserRepository userRepository, EntryMapper entryMapper) {
    this.entryRepository = entryRepository;
    this.userRepository = userRepository;
    this.entryMapper = entryMapper;
  }

  public List<EntryResponseDto> getEntries() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    String userEmail = authentication.getName();
    User user = findUserByEmail(userEmail);

    List<Entry> entries;

    if (user.getRole() == Role.ADMIN) {
      entries = entryRepository.findAll();
    } else {
      entries = entryRepository.findByUser(user);
    }

    return entries
      .stream()
      .map(entryMapper::toResponseDto)
      .toList();
  }

  public EntryResponseDto getEntryByUuid(String uuid) {
    Entry entry = findEntryByUuid(uuid);
    validateEntryOwnership(entry);
    
    return entryMapper.toResponseDto(entry);
  }

  public EntryResponseDto createEntry(EntryRequestDto entryRequestDto) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    String userEmail = authentication.getName();
    User user = findUserByEmail(userEmail);

    Entry entry = new Entry();
    entry.setUser(user);
    entry.setDescription(entryRequestDto.description());
    entry.setDate(entryRequestDto.date());

    return entryMapper.toResponseDto(entryRepository.save(entry));
  }

  public EntryResponseDto updateEntry(String uuid, EntryRequestDto entryRequestDto) {
    Entry entry = findEntryByUuid(uuid);
    validateEntryOwnership(entry);

    if (entryRequestDto.description() != null && !entryRequestDto.description().isBlank()) {
      entry.setDescription(entryRequestDto.description());
    }

    if (entryRequestDto.date() != null) {
      entry.setDate(entryRequestDto.date());
    }

    return entryMapper.toResponseDto(entryRepository.save(entry));
  }

  public void deleteEntry(String uuid) {
    Entry entry = findEntryByUuid(uuid);
    validateEntryOwnership(entry);
    
    entryRepository.delete(entry);
  }

  private void validateEntryOwnership(Entry entry) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String userEmail = authentication.getName();
    User user = findUserByEmail(userEmail);

    boolean isAdmin = authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    if (!entry.getUser().getUuid().equals(user.getUuid()) && !isAdmin) {
      throw new AccessDeniedException("You can only read, modify or delete your own entries");
    }
  }

  private User findUserByEmail(String email) {
    return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User with email: " + email + " not found"));
  }

  private Entry findEntryByUuid(String uuid) {
    return entryRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException("Entry not found"));
  }
}
