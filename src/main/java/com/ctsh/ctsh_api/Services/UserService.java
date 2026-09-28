package com.ctsh.ctsh_api.Services;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ctsh.ctsh_api.Dtos.UserResponseDto;
import com.ctsh.ctsh_api.Dtos.Validation.UserRequestDto;
import com.ctsh.ctsh_api.Exceptions.ResourceAlreadyExistsException;
import com.ctsh.ctsh_api.Exceptions.ResourceNotFoundException;
import com.ctsh.ctsh_api.Models.Enum.Role;
import com.ctsh.ctsh_api.Models.User;
import com.ctsh.ctsh_api.Repositories.UserRepository;

@Service
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final FileService fileService;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, FileService fileService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.fileService = fileService;
  }

  public List<UserResponseDto> getAllUsers() {
    return userRepository.findAll()
        .stream()
        .map(this::toResponseDto)
        .toList();
  }

  public UserResponseDto getUserById(String uuid) {
    User user = findUser(uuid);
    requireSameUserOrAdmin(user.getEmail());
    return toResponseDto(user);
  }

  public UserResponseDto createUser(UserRequestDto dto) {
    User user = new User();
    String passwordHash = passwordEncoder.encode(dto.password());

    if (userRepository.findByEmail(dto.email()).isPresent()) {
      throw new ResourceAlreadyExistsException("Email already registered: " + dto.email());
    }

    this.fileService.validateFile(dto.profilePicture());
    String profilePicture = this.fileService.saveFile(dto.profilePicture());

    
    user.setName(dto.name());
    user.setEmail(dto.email());
    user.setPassword(passwordHash);
    user.setProfilePicture(profilePicture);
    user.setRole(Role.USER);
    return toResponseDto(userRepository.save(user));
  }

  public UserResponseDto updateUser(String uuid, UserRequestDto dto) {
    User user = findUser(uuid);
    requireSameUserOrAdmin(user.getEmail());


    if (dto.profilePicture() != null && !dto.profilePicture().isEmpty()) {
      this.fileService.validateFile(dto.profilePicture());
      String newProfilePicture = this.fileService.updateFile(dto.profilePicture(), user.getProfilePicture());
      user.setProfilePicture(newProfilePicture);
    }

    if (dto.name() != null && !dto.name().isBlank()) {
      user.setName(dto.name());
    }

    return toResponseDto(userRepository.save(user));
  }

  public void deleteUser(String uuid) {
    User user = findUser(uuid);
    requireSameUserOrAdmin(user.getEmail());

    if (user.getProfilePicture() != null) {
      fileService.deleteFile(user.getProfilePicture());
    }

    userRepository.delete(findUser(uuid));
  }

  private User findUser(String uuid) {
    return userRepository.findById(uuid)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with uuid: " + uuid));
  }

  private UserResponseDto toResponseDto(User user) {
    UserResponseDto dto = new UserResponseDto();
    dto.setUuid(user.getUuid());
    dto.setName(user.getName());
    dto.setEmail(user.getEmail());
    dto.setRole(user.getRole());
    dto.setProfilePicture(fileService.getPublicUrl(user.getProfilePicture()));
    return dto;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
        
    return org.springframework.security.core.userdetails.User
      .withUsername(user.getEmail())
      .password(user.getPassword())
      .authorities("ROLE_" + user.getRole().name())
      .build();

  }

  private void requireSameUserOrAdmin(String ownerEmail) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    boolean isAdmin = auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    if (!isAdmin && !ownerEmail.equals(auth.getName())) {
      throw new AccessDeniedException("You can only read, modify or delete your own user");
    }
  }
}