package com.ctsh.ctsh_api.Dtos.Validation;

import com.ctsh.ctsh_api.Dtos.Validation.ValidationGroups.OnCreate;
import com.ctsh.ctsh_api.Dtos.Validation.ValidationGroups.OnUpdate;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record MailRequestDto(
  @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "The sender user cannot be null")
  @NotEmpty(groups = {OnUpdate.class}, message = "The sender user cannot be empty")
  String fromUuuid,

  @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "The receiver user cannot be null")
  @NotEmpty(groups = {OnUpdate.class}, message = "The receiver user cannot be empty")
    String toUuuid,

  @NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "The message cannot be null")
  @NotEmpty(groups = {OnUpdate.class}, message = "The message cannot be empty")
    String text
) {}
