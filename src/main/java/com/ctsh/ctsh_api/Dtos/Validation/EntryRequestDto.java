package com.ctsh.ctsh_api.Dtos.Validation;

import java.time.LocalDate;

import com.ctsh.ctsh_api.Dtos.Validation.ValidationGroups.OnCreate;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EntryRequestDto (
  @NotNull( groups = {OnCreate.class}, message = "The date cannot be null")
  @JsonFormat( pattern = "dd/MM/yyyy")
  LocalDate date, 

  @NotBlank(groups = {OnCreate.class}, message = "The description cannot be blank")
  String description
){}
