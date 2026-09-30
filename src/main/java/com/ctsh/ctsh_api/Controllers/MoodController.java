package com.ctsh.ctsh_api.Controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ctsh.ctsh_api.Dtos.ApiResponse;
import com.ctsh.ctsh_api.Services.MoodService;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;



@RestController
@RequestMapping("/mood")
public class MoodController {

  private final MoodService moodService;

  public MoodController(MoodService moodService) {
    this.moodService = moodService;
  }
  @GetMapping
  public ResponseEntity<ApiResponse> getMoods() {
      return ResponseEntity.status(HttpStatus.OK).body(
        ApiResponse.of(HttpStatus.OK,"Moods retrieved successfully", this.moodService.getAllMoods()
        ));
  }
  
  @GetMapping("/{name}")
  public ResponseEntity<ApiResponse> getMood(@PathVariable String name) {
      return ResponseEntity.status(HttpStatus.OK).body(
        ApiResponse.of(HttpStatus.OK,"Mood retrieved successfully", this.moodService.getMoodByName(name)
        ));
  }

  @PostMapping("/{name}")
  public ResponseEntity<ApiResponse> increaseMoodCount(@PathVariable String name) {
      return ResponseEntity.status(HttpStatus.OK).body(
        ApiResponse.of(HttpStatus.OK,"Mood count incremented successfully", this.moodService.incrementMoodCount(name)
        ));
  }

  @DeleteMapping("/{name}")
  public ResponseEntity<ApiResponse> deleteMood(@PathVariable String name) {
    this.moodService.deleteMoodByName(name);
    return ResponseEntity.noContent().build();
  }
  
  
}
