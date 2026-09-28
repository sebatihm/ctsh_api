package com.ctsh.ctsh_api.Dtos;

import java.time.Instant;

import org.springframework.http.HttpStatus;

public record ApiResponse(
    Instant timestamp,
    int status,
    String message,
    Object data) {

  public static ApiResponse of(HttpStatus status, String message, Object data) {
    return new ApiResponse(
        Instant.now(),
        status.value(),
        message,
        data);
  }
}