package com.ctsh.ctsh_api.Controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ctsh.ctsh_api.Dtos.ApiResponse;
import com.ctsh.ctsh_api.Dtos.Validation.EntryRequestDto;
import com.ctsh.ctsh_api.Services.EntryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

import com.ctsh.ctsh_api.Dtos.Validation.ValidationGroups.OnCreate;
import com.ctsh.ctsh_api.Dtos.Validation.ValidationGroups.OnUpdate;


@RestController
@RequestMapping("/entry")
public class EntryController {

  private final EntryService entryService;

  public EntryController(EntryService entryService) {
    this.entryService = entryService;
  }

  @GetMapping
  public ResponseEntity<ApiResponse> getAllEntries() {
    return ResponseEntity.status(HttpStatus.OK).body(
      ApiResponse.of( HttpStatus.OK, "Entries retrieved successfully", entryService.getEntries()
      ));
  }

  @GetMapping("/{uuid}")
  public ResponseEntity<ApiResponse> getEntryByUuid(@PathVariable String uuid) {
    return ResponseEntity.status(HttpStatus.OK).body(
      ApiResponse.of( HttpStatus.OK, "Entry retrieved successfully", entryService.getEntryByUuid(uuid)
      ));
  }

  @PostMapping
  public ResponseEntity<ApiResponse> createEntry(@Validated(OnCreate.class) @RequestBody EntryRequestDto dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(
      ApiResponse.of( HttpStatus.CREATED, "Entry created successfully", entryService.createEntry(dto)
      ));
  }

  @PutMapping("/{uuid}")
  public ResponseEntity<ApiResponse> updateEntry(@PathVariable String uuid, @Validated(OnUpdate.class) @RequestBody EntryRequestDto dto) {
    return ResponseEntity.status(HttpStatus.OK).body(
      ApiResponse.of( HttpStatus.OK, "Entry updated successfully", entryService.updateEntry(uuid, dto)
      ));
  }

  @DeleteMapping("/{uuid}")
  public ResponseEntity<ApiResponse> deleteEntry(@PathVariable String uuid) {
    entryService.deleteEntry(uuid);
    return ResponseEntity.noContent().build();
  }
  
  
  
  
}
