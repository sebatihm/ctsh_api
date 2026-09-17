package com.ctsh.ctsh_api.Controllers.Error;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ctsh.ctsh_api.config.Auth.ApiError;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class CustomErrorController implements ErrorController {
  @RequestMapping("/error")
  public ResponseEntity<ApiError> error(HttpServletRequest request) {
    Integer rawStatus = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    int status = rawStatus != null ? rawStatus : 404;

    String rawMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
    String path = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
    if (path == null) path = request.getRequestURI();

    String message = status >= 500 ? "Unexpected server error"      
        : (rawMessage != null && !rawMessage.isBlank() ? rawMessage : "An error occurred");

    if (status >= 500) {
      log.error("Unhandled error on {}: {}", path, rawMessage != null ? rawMessage : "(no message)");
    }

    HttpStatus httpStatus = HttpStatus.resolve(status);
    String error = httpStatus != null ? httpStatus.getReasonPhrase() : "Error";
    
    return ResponseEntity.status(status)
        .body(new ApiError(Instant.now(), status, error, message, path));
  }
}
