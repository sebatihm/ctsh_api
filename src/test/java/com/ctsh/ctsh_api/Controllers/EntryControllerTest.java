package com.ctsh.ctsh_api.Controllers;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ctsh.ctsh_api.Dtos.EntryResponseDto;
import com.ctsh.ctsh_api.Dtos.UserResponseDto;
import com.ctsh.ctsh_api.Dtos.Validation.EntryRequestDto;
import com.ctsh.ctsh_api.Models.Enum.Role;
import com.ctsh.ctsh_api.Services.EntryService;

@WebMvcTest(controllers = EntryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class EntryControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private EntryService entryService;

  private UserResponseDto user() {
    UserResponseDto user = new UserResponseDto();
    user.setUuid("user-uuid");
    user.setName("John Doe");
    user.setEmail("test@email.com");
    user.setRole(Role.USER);
    user.setProfilePicture("test.jpg");
    return user;
  }

  private EntryResponseDto entry(UserResponseDto user) {
    EntryResponseDto entry = new EntryResponseDto();
    entry.setUuid("uuid-1234");
    entry.setDate("10/10/2010");
    entry.setUser(user);
    entry.setDescription("This is a test entry.");
    return entry;
  }

  @Test
  void testGetEntries() throws Exception {
    UserResponseDto user = user();
    EntryResponseDto entry = entry(user);

    when(entryService.getEntries()).thenReturn(List.of(entry));

    mockMvc.perform(get("/entry"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[0].uuid").value("uuid-1234"));
  }

  @Test
  void testGetEntryByUuid() throws Exception {
    UserResponseDto user = user();
    EntryResponseDto entry = entry(user);

    when(entryService.getEntryByUuid("uuid-1234")).thenReturn(entry);

    mockMvc.perform(get("/entry/uuid-1234"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.uuid").value("uuid-1234"));
  }

  @Test
  void testCreateEntry() throws Exception {
    UserResponseDto user = user();
    EntryResponseDto entryResponse = entry(user);

    EntryRequestDto entry = new EntryRequestDto(
      LocalDate.of(2010, 10, 10),
      "This is a test entry."
    );

    when(entryService.createEntry(entry)).thenReturn(entryResponse);

    mockMvc.perform(post("/entry")
      .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "date": "10/10/2010",
              "description": "This is a test entry."
            }
            """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.uuid").value("uuid-1234"));
  }

  @Test
  void testCreateEntryWithOutDate() throws Exception {
    mockMvc.perform(post("/entry")
      .contentType(MediaType.APPLICATION_JSON)
      .content("""
        {
          "description": "This is a test entry.",
        }
        """))
      .andExpect(status().isBadRequest());
  }

  @Test
  void testCreateEntryWithOutValidDate() throws Exception {
    mockMvc.perform(post("/entry")
      .contentType(MediaType.APPLICATION_JSON)
      .content("""
        {
          "date": "10-10-2010",
        }
        """))
      .andExpect(status().isBadRequest());
  }


  @Test
  void testCreateEntryWithOutDescription() throws Exception {
    mockMvc.perform(post("/entry")
      .contentType(MediaType.APPLICATION_JSON)
      .content("""
        {
          "description": "This is a test entry.",
        }
        """))
      .andExpect(status().isBadRequest());
  }

  @Test
  void testUpdateEntry() throws Exception {
    UserResponseDto user = user();
    EntryResponseDto entryResponse = entry(user);

    EntryRequestDto entry = new EntryRequestDto(
      LocalDate.of(2010, 10, 11),
      "This is a updated entry."
    );

    when(entryService.updateEntry("uuid-1234", entry)).thenReturn(entryResponse);

    mockMvc.perform(put("/entry/uuid-1234")
      .contentType(MediaType.APPLICATION_JSON)
      .content("""
        {
          "date" : "11/10/2010",
          "description": "This is a updated entry."
        }
        """))
      .andExpect(status().isOk());
  }

  @Test
  void testUpdatePartialEntry() throws Exception {
    UserResponseDto user = user();
    EntryResponseDto entryResponse = entry(user);

    EntryRequestDto entry = new EntryRequestDto(
      null,
      "Updated description"
    );

    when(entryService.updateEntry("uuid-1234", entry)).thenReturn(entryResponse);
    mockMvc.perform(put("/entry/uuid-1234")
      .contentType(MediaType.APPLICATION_JSON)
      .content("""
        {
          "description": "Updated description"
        }
        """))
      .andExpect(status().isOk());
  }

  @Test
  void testDeleteEntry() throws Exception {
    mockMvc.perform(delete("/entry/uuid-1234"))
      .andExpect(status().isNoContent());
  }



}
