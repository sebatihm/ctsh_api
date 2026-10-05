package com.ctsh.ctsh_api.Controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.ctsh.ctsh_api.Dtos.MoodResponseDto;
import com.ctsh.ctsh_api.Exceptions.BadRequestException;
import com.ctsh.ctsh_api.Services.MoodService;

@WebMvcTest(controllers = MoodController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MoodControllerTest {
  @Autowired
  private MockMvc mockMvc;
  
  @MockitoBean
  private MoodService moodService;

  private MoodResponseDto mood() {
    MoodResponseDto mood = new MoodResponseDto();
    mood.setName("Happy");
    mood.setCount(10);
    return mood;
  }

  @Test
  void testGetAllMoods() throws Exception {
    when(moodService.getAllMoods()).thenReturn(List.of(mood()));

    mockMvc.perform(get("/mood"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[0].name").value("Happy"))
      .andExpect(jsonPath("$.data[0].count").value(10));
    
    verify(moodService).getAllMoods();
  }

  @Test
  void testGetMoodByName() throws Exception {
    when(moodService.getMoodByName("Happy")).thenReturn(mood());

    mockMvc.perform(get("/mood/Happy"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.name").value("Happy"))
      .andExpect(jsonPath("$.data.count").value(10));
    
    verify(moodService).getMoodByName("Happy");
  }

  @Test
  void testGetUnknownMoodByName() throws Exception {
    when(moodService.getMoodByName("Unknown")).thenThrow(new BadRequestException("Mood not found"));

    mockMvc.perform(get("/mood/Unknown"))
      .andExpect(status().isBadRequest());
    
    verify(moodService).getMoodByName("Unknown");
  }

  @Test
  void testIncreaseMoodCount() throws Exception {
    when(moodService.incrementMoodCount("Happy")).thenReturn(mood());

    mockMvc.perform(post("/mood/Happy"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.name").value("Happy"))
      .andExpect(jsonPath("$.data.count").value(10));
    
    verify(moodService).incrementMoodCount("Happy");
  }

  @Test
  void testDeleteMood() throws Exception {
    mockMvc.perform(delete("/mood/Happy"))
      .andExpect(status().isNoContent());
    
    verify(moodService).deleteMoodByName("Happy");
  }

  @Test
  void testDeleteUnknownMood() throws Exception {
    doThrow(new BadRequestException("Mood not found"))
      .when(moodService)
      .deleteMoodByName("Unknown");
      
    mockMvc.perform(delete("/mood/Unknown"))
      .andExpect(status().isBadRequest());
    
    verify(moodService).deleteMoodByName("Unknown");
  }


}
