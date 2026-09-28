package com.ctsh.ctsh_api.Exceptions;

import org.springframework.http.HttpStatus;

public class ResourceAlreadyExistsException extends ApiException {
  public ResourceAlreadyExistsException(String message) {
    super(HttpStatus.CONFLICT, message);
  }
}
