package com.ctsh.ctsh_api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.ctsh.ctsh_api.Models.Mood;
import com.ctsh.ctsh_api.Models.User;
import com.ctsh.ctsh_api.Models.Enum.Role;
import com.ctsh.ctsh_api.Repositories.MoodRepository;
import com.ctsh.ctsh_api.Repositories.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


import org.junit.jupiter.api.Test;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MoodIntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private MoodRepository moodRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;



  private MvcResult testUserLogin(String role) throws Exception {
    User user = new User();

    user.setName("John Doe");
    user.setEmail("john@example.com");
    user.setProfilePicture("profile.jpg");
    user.setPassword(passwordEncoder.encode("password123"));
    user.setRole( role == "ADMIN" ? Role.ADMIN : Role.USER);

    userRepository.save(user);

    String body = """
        {
            "email": "john@example.com",
            "password": "password123"
        }
      """;
    return mockMvc.perform(
      post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body)
    ).andExpect(status().isOk())
    .andReturn();
  }

  @Test
  void testMoodEndpoint() throws Exception {

    mockMvc.perform(
      post("/mood/happy")
        .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isOk())
    .andExpect(jsonPath("$.data.name").value("happy"))
    .andExpect(jsonPath("$.data.count").value(1))
    .andReturn();
  }

  @Test
  void testMoodIncrementEndpoint() throws Exception {

    Mood mood = new Mood();
    mood.setName("happy");
    mood.setCount(5);
    moodRepository.save(mood);

    mockMvc.perform(
      post("/mood/happy")
        .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isOk())
    .andExpect(jsonPath("$.data.name").value("happy"))
    .andExpect(jsonPath("$.data.count").value(6))
    .andReturn();
  }

  @Test
  void testGetMood() throws Exception {
    MvcResult loginResult = testUserLogin("USER");
    String token = loginResult.getResponse().getCookie("jwt").getValue();

    Mood mood = new Mood();
    mood.setName("happy");
    mood.setCount(5);
    moodRepository.save(mood);

    mockMvc.perform(
      get("/mood/happy")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isOk())
    .andExpect(jsonPath("$.data.name").value("happy"))
    .andExpect(jsonPath("$.data.count").value(5))
    .andReturn();
  }

  @Test
  void testGetMoods() throws Exception {
    MvcResult loginResult = testUserLogin("USER");
    String token = loginResult.getResponse().getCookie("jwt").getValue();
    Mood mood = new Mood();
    mood.setName("happy");
    mood.setCount(5);
    moodRepository.save(mood);

    mockMvc.perform(
      get("/mood")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isOk())
    .andExpect(jsonPath("$.data[0].name").value("happy"))
    .andExpect(jsonPath("$.data[0].count").value(5))
    .andReturn();
  }

  @Test
  void testGetMoodsWithNoAuthorization() throws Exception {
    Mood mood = new Mood();
    mood.setName("happy");
    mood.setCount(5);
    moodRepository.save(mood);

    mockMvc.perform(
      get("/mood")
        .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isUnauthorized())
    .andReturn();
  }

  @Test
  void testDeleteMoodWithAdminRights() throws Exception {
    MvcResult loginResult = testUserLogin("ADMIN");
    String token = loginResult.getResponse().getCookie("jwt").getValue();
    Mood mood = new Mood();
    mood.setName("happy");
    mood.setCount(5);
    moodRepository.save(mood);

    mockMvc.perform(
      delete("/mood/happy")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isNoContent())
    .andReturn();
  }

  @Test
  void testDeleteMoodWithoutAdminRights() throws Exception {
    MvcResult loginResult = testUserLogin("USER");
    String token = loginResult.getResponse().getCookie("jwt").getValue();
    Mood mood = new Mood();
    mood.setName("happy");
    mood.setCount(5);
    moodRepository.save(mood);

    mockMvc.perform(
      delete("/mood/happy")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isForbidden())
    .andReturn();
  }

}
