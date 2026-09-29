package com.ctsh.ctsh_api.Exceptions;

public class BadRequestException extends ApiException {
  public BadRequestException(String message) {
    super(org.springframework.http.HttpStatus.BAD_REQUEST, message);
  }
  
}
