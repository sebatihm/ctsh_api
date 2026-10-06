package com.ctsh.ctsh_api.Dtos.Validation;

import com.ctsh.ctsh_api.Dtos.Validation.ValidationGroups.OnCreate;

import jakarta.validation.constraints.NotBlank;


public record MailRequestDto(
  @NotBlank(groups = {OnCreate.class}, message = "The sender user cannot be blank")
  String fromUuid,

  @NotBlank(groups = {OnCreate.class}, message = "The receiver user cannot be blank")
  String toUuid,

  @NotBlank(groups = {OnCreate.class}, message = "The text cannot be blank")
  String text
) {}
