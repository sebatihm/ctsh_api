package com.ctsh.ctsh_api.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ctsh.ctsh_api.Dtos.ApiResponse;
import com.ctsh.ctsh_api.Dtos.Validation.UserRequestDto;
import com.ctsh.ctsh_api.Services.UserService;

import com.ctsh.ctsh_api.Dtos.Validation.ValidationGroups.OnCreate;
import com.ctsh.ctsh_api.Dtos.Validation.ValidationGroups.OnUpdate;


@RestController
@RequestMapping("/user")
public class UserController {

  @Autowired 
  private UserService userService;


  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ResponseEntity<ApiResponse> getAllUsers() {
    return ResponseEntity.status(HttpStatus.OK).body(
      ApiResponse.of(HttpStatus.OK, "Users retrieved successfully", userService.getAllUsers())
      );
  }

  @GetMapping("/{uuid}")
  public ResponseEntity<ApiResponse> getUserById(@PathVariable String uuid) {
    return ResponseEntity.status(HttpStatus.OK).body(
      ApiResponse.of(HttpStatus.OK,"User retrieved successfully", userService.getUserById(uuid))
    );
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse> createUser(@Validated(OnCreate.class) @ModelAttribute UserRequestDto dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(
      ApiResponse.of(HttpStatus.CREATED,"User created successfully", userService.createUser(dto))
    );
  }

  @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, path = "/{uuid}")
  public ResponseEntity<ApiResponse> updateUser(@PathVariable String uuid, @Validated(OnUpdate.class) @ModelAttribute UserRequestDto dto) {
    return ResponseEntity.status(HttpStatus.OK).body(
      ApiResponse.of(HttpStatus.OK,"User updated successfully", userService.updateUser(uuid, dto))
    );
  }

  @DeleteMapping("/{uuid}")
  public ResponseEntity<Void> deleteUser(@PathVariable String uuid) {
    userService.deleteUser(uuid);
    return ResponseEntity.noContent().build();
  }
}