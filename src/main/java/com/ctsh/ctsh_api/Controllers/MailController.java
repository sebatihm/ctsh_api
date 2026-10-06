package com.ctsh.ctsh_api.Controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ctsh.ctsh_api.Dtos.ApiResponse;
import com.ctsh.ctsh_api.Dtos.Validation.MailRequestDto;
import com.ctsh.ctsh_api.Services.MailService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ctsh.ctsh_api.Dtos.Validation.ValidationGroups.OnCreate;
import com.ctsh.ctsh_api.Dtos.Validation.ValidationGroups.OnUpdate;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/mail")
public class MailController {

  private final MailService mailService;

  public MailController(MailService mailService) {
    this.mailService = mailService;
  }

  @GetMapping
  public ResponseEntity<ApiResponse> getAllMails() {
    return ResponseEntity.status(HttpStatus.OK).body(
      ApiResponse.of(HttpStatus.OK,"Mails retrieved successfully", this.mailService.getMails()
    ));
  }

  @GetMapping("/{uuid}")
  public ResponseEntity<ApiResponse> getMailByUuid(@PathVariable String uuid) {
      return ResponseEntity.status(HttpStatus.OK).body(
        ApiResponse.of(HttpStatus.OK,"Mail retrieved successfully", this.mailService.getMailByUuid(uuid)
      ));
  }

  @PostMapping
  public ResponseEntity<ApiResponse> createMail(@Validated(OnCreate.class) @RequestBody MailRequestDto dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(
      ApiResponse.of(HttpStatus.CREATED,"Mail created successfully", this.mailService.createMail(dto)
    ));
  }

  @PutMapping("/{uuid}")
  public ResponseEntity<ApiResponse> updateMail(@PathVariable String uuid, @Validated(OnUpdate.class) @RequestBody MailRequestDto dto) {
    return ResponseEntity.status(HttpStatus.OK).body(
      ApiResponse.of(HttpStatus.OK,"Mail updated successfully", this.mailService.updateMail(uuid, dto)
    ));
  }

  @DeleteMapping("/{uuid}")
  public ResponseEntity<Void> deleteMail(@PathVariable String uuid) {
    this.mailService.deleteMail(uuid);
    return ResponseEntity.noContent().build();
  }
  
  
  
  
}
