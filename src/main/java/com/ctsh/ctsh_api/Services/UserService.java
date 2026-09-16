package com.ctsh.ctsh_api.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ctsh.ctsh_api.Dtos.UserRequestDto;
import com.ctsh.ctsh_api.Dtos.UserResponseDto;
import com.ctsh.ctsh_api.Models.Enum.Role;
import com.ctsh.ctsh_api.Models.User;
import com.ctsh.ctsh_api.Repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

  @Autowired
  private UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<UserResponseDto> getAllUsers() {
    return userRepository.findAll().stream()
        .map(this::toResponseDto)
        .toList();
  }

  public UserResponseDto getUserById(String uuid) {
    return toResponseDto(findUser(uuid));
  }

  //todo: add encription for password
  public UserResponseDto createUser(UserRequestDto dto) {
    User user = new User();
    user.setName(dto.getName());
    user.setEmail(dto.getEmail());
    user.setPassword(dto.getPassword());
    user.setProfilePicture(dto.getProfilePicture());
    user.setRole(Role.USER);
    return toResponseDto(userRepository.save(user));
  }

  public UserResponseDto updateUser(String uuid, UserRequestDto dto) {
    User user = findUser(uuid);
    user.setName(dto.getName());
    user.setEmail(dto.getEmail());
    user.setPassword(dto.getPassword());
    user.setProfilePicture(dto.getProfilePicture());
    return toResponseDto(userRepository.save(user));
  }

  public void deleteUser(String uuid) {
    userRepository.delete(findUser(uuid));
  }

  private User findUser(String uuid) {
    return userRepository.findById(uuid)
        .orElseThrow(() -> new RuntimeException("User not found with uuid: " + uuid));
  }

  private UserResponseDto toResponseDto(User user) {
    UserResponseDto dto = new UserResponseDto();
    dto.setUuid(user.getUuid());
    dto.setName(user.getName());
    dto.setEmail(user.getEmail());
    dto.setRole(user.getRole());
    dto.setProfilePicture(user.getProfilePicture());
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
}