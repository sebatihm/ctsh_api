package com.ctsh.ctsh_api.Dtos.Validation;

import org.springframework.web.multipart.MultipartFile;

import com.ctsh.ctsh_api.Dtos.Validation.ValidationGroups.OnCreate;
import com.ctsh.ctsh_api.Dtos.Validation.ValidationGroups.OnUpdate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record UserRequestDto (
  @NotBlank(groups = {OnCreate.class}, message = "Name cannot be blank")
  String name,

  @NotBlank(groups = {OnCreate.class}, message = "Email cannot be blank")
  @Email(groups = {OnCreate.class, OnUpdate.class},message = "Email should be valid")
  String email,

  @NotEmpty(groups = {OnCreate.class}, message = "Password cannot be empty")
  @NotBlank(groups = {OnCreate.class}, message = "Password cannot be blank")
  @Size(groups = {OnCreate.class}, min = 8, message = "Password must be at least 8 characters long")
  String password,

  @NotNull(groups = {OnCreate.class}, message = "Profile picture cannot be null")
  MultipartFile profilePicture
){ }