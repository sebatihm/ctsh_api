package com.ctsh.ctsh_api.Controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ctsh.ctsh_api.Dtos.UserRequestDto;
import com.ctsh.ctsh_api.Dtos.UserResponseDto;
import com.ctsh.ctsh_api.Services.UserService;

import lombok.RequiredArgsConstructor;

@RestController("users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping
  public List<UserResponseDto> getAllUsers() {
    return userService.getAllUsers();
  }

  @GetMapping("/{uuid}")
  public UserResponseDto getUserById(@PathVariable String uuid) {
    return userService.getUserById(uuid);
  }

  @PostMapping
  public ResponseEntity<UserResponseDto> createUser(@RequestBody UserRequestDto dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto));
  }

  @PutMapping("/{uuid}")
  public UserResponseDto updateUser(@PathVariable String uuid, @RequestBody UserRequestDto dto) {
    return userService.updateUser(uuid, dto);
  }

  @DeleteMapping("/{uuid}")
  public ResponseEntity<Void> deleteUser(@PathVariable String uuid) {
    userService.deleteUser(uuid);
    return ResponseEntity.noContent().build();
  }
}