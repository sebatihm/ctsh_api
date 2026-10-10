package com.ctsh.ctsh_api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.ctsh.ctsh_api.Models.Entry;
import com.ctsh.ctsh_api.Models.User;
import com.ctsh.ctsh_api.Models.Enum.Role;
import com.ctsh.ctsh_api.Repositories.EntryRepository;
import com.ctsh.ctsh_api.Repositories.UserRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class EntryIntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntryRepository entryRepository;


  @Autowired
  private PasswordEncoder passwordEncoder;

  private Entry createTestEntry(User user) {
    Entry entry = new Entry();
    entry.setDate(LocalDate.of(2023, 10, 10));
    entry.setDescription("This is a test entry.");
    entry.setUser(user);
    entryRepository.save(entry);
    return entry;
  }

  private User createUserTest(String role, String email) {
    User user = new User();

    user.setName("John Doe");
    user.setEmail(email);
    user.setProfilePicture("profile.jpg");
    user.setPassword(passwordEncoder.encode("password123"));
    user.setRole( role == "ADMIN" ? Role.ADMIN : Role.USER);

    userRepository.save(user);

    return user;
  }

  private MvcResult loginTestUser(User user) throws Exception {


    String body = """
    {
        "email": "%s",
        "password": "password123"
    }
    """.formatted(user.getEmail());

    return mockMvc.perform(
      post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body)
    ).andExpect(status().isOk())
    .andReturn();
  }

  @Test
  void testGetEntriesSuccessfully() throws Exception {
    User user = createUserTest("USER", "john@example.com");

    createTestEntry(createUserTest("USER", "jane@example.com"));
    createTestEntry(user);
    
    MvcResult result = loginTestUser(user);
    String token = result.getResponse().getCookie("jwt").getValue();

    mockMvc.perform(
      get("/entry")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isOk())
    .andExpect(jsonPath("$.data[0].user.email").value("john@example.com"))
    .andReturn();
  }

  @Test
  void testGetEntriesSuccessfullyAdmin() throws Exception {
    User user = createUserTest("ADMIN", "john@example.com");

    createTestEntry(createUserTest("USER", "jane@example.com"));
    createTestEntry(user);
    
    MvcResult result = loginTestUser(user);
    String token = result.getResponse().getCookie("jwt").getValue();

    mockMvc.perform(
      get("/entry")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isOk())
    .andExpect(jsonPath("$.data[0].user.email").value("jane@example.com"))
    .andExpect(jsonPath("$.data[1].user.email").value("john@example.com"))
    .andReturn();
  }

  @Test
  void testGetEntryByIdSuccessfully() throws Exception {
    User user = createUserTest("USER", "john@example.com");
    Entry entry = createTestEntry(user);

    MvcResult result = loginTestUser(user);
    String token = result.getResponse().getCookie("jwt").getValue();

    mockMvc.perform(
      get("/entry/" + entry.getUuid())
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isOk())
    .andExpect(jsonPath("$.data.user.email").value("john@example.com"))
    .andReturn();
  }

  @Test
  void testGetEntryByIdNotFound() throws Exception {
    User user = createUserTest("USER", "john@example.com");

    MvcResult result = loginTestUser(user);
    String token = result.getResponse().getCookie("jwt").getValue();

    mockMvc.perform(
      get("/entry/non-existent-uuid")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isNotFound())
    .andReturn();
  }

  @Test
  void testGetEntryWithInvalidUser() throws Exception {
    User user1 = createUserTest("USER", "john@example.com");
    User user2 = createUserTest("USER", "jane@example.com");
    Entry entry = createTestEntry(user1);

    MvcResult result = loginTestUser(user2);
    String token = result.getResponse().getCookie("jwt").getValue();

    mockMvc.perform(
      get("/entry/" + entry.getUuid())
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isForbidden())
    .andReturn();
  }

  @Test
  void testGetEntryWithAdminUser() throws Exception {
    User user1 = createUserTest("USER", "john@example.com");
    User admin = createUserTest("ADMIN", "jane@example.com");
    Entry entry = createTestEntry(user1);

    MvcResult result = loginTestUser(admin);
    String token = result.getResponse().getCookie("jwt").getValue();

    mockMvc.perform(
      get("/entry/" + entry.getUuid())
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isOk())
    .andExpect(jsonPath("$.data.user.email").value("john@example.com"));
  }

  @Test
  void testCreateEntrySuccessfully() throws Exception {
    User user = createUserTest("USER", "john@example.com");

    MvcResult result = loginTestUser(user);
    String token = result.getResponse().getCookie("jwt").getValue();

    mockMvc.perform(
      post("/entry")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
              "date": "10/10/2023",
              "description": "This is a test entry."
          }
          """)
    ).andExpect(status().isCreated())
    .andExpect(jsonPath("$.data.date").value("10/10/2023"))
    .andExpect(jsonPath("$.data.description").value("This is a test entry."))
    .andReturn();
  }

  @Test
  void testCreateEntryWithOutDescription() throws Exception {
    User user = createUserTest("USER", "john@example.com");

    MvcResult result = loginTestUser(user);
    String token = result.getResponse().getCookie("jwt").getValue();

    mockMvc.perform(
      post("/entry")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
          "date": "10/10/2023",
          }
          """)
    ).andExpect(status().isBadRequest())
    .andReturn();
  }

  @Test
  void testCreateEntryWithOutDate() throws Exception {
    User user = createUserTest("USER", "john@example.com");

    MvcResult result = loginTestUser(user);
    String token = result.getResponse().getCookie("jwt").getValue();

    mockMvc.perform(
      post("/entry")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "description": "This is a test entry.",
          }
          """)
    ).andExpect(status().isBadRequest())
    .andReturn();
  }
  
  @Test
  void testCreateEntryWithInvalidDate() throws Exception {
    User user = createUserTest("USER", "john@example.com");

    MvcResult result = loginTestUser(user);
    String token = result.getResponse().getCookie("jwt").getValue();

    mockMvc.perform(
      post("/entry")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "description": "This is a test entry.",
            "date": "10-10-2004"
          }
          """)
    ).andExpect(status().isBadRequest())
    .andReturn();
  }

  @Test
  void testUpdateEntryByIdSuccessfully() throws Exception {
    User user = createUserTest("USER", "john@example.com");
    Entry entry = createTestEntry(user);

    MvcResult result = loginTestUser(user);
    String token = result.getResponse().getCookie("jwt").getValue();

    mockMvc.perform(
      put("/entry/" + entry.getUuid())
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "description": "Update entry",
            "date": "09/03/2017"
          }
          """)
    ).andExpect(status().isOk())
    .andExpect(jsonPath("$.data.date").value("09/03/2017"))
    .andExpect(jsonPath("$.data.description").value("Update entry"))
    .andReturn();
  }

  @Test
  void testUpdateEntryByIdPartiallySuccessfully() throws Exception {
    User user = createUserTest("USER", "john@example.com");
    Entry entry = createTestEntry(user);

    MvcResult result = loginTestUser(user);
    String token = result.getResponse().getCookie("jwt").getValue();

    mockMvc.perform(
      put("/entry/" + entry.getUuid())
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "description": "Update entry"
          }
          """)
    ).andExpect(status().isOk())
    .andExpect(jsonPath("$.data.description").value("Update entry"))
    .andReturn();
  }

  @Test
  void testUpdateEntryWithoutExistingData() throws Exception {
    User user = createUserTest("USER", "john@example.com");

    MvcResult result = loginTestUser(user);
    String token = result.getResponse().getCookie("jwt").getValue();

    mockMvc.perform(
      put("/entry/non-existent-uuid")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "description": "Update entry",
            "date": "09/03/2017"
          }
          """)
    ).andExpect(status().isNotFound())
    .andReturn();
  }

  @Test
  void testUpdateEntryWithInvalidUser() throws Exception {
    User user = createUserTest("USER", "john@example.com");
    Entry entry = createTestEntry(user);

    MvcResult result = loginTestUser(createUserTest("USER", "jane@example.com"));
    String token = result.getResponse().getCookie("jwt").getValue();

    mockMvc.perform(
      put("/entry/" + entry.getUuid())
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "description": "Update entry",
            "date": "09/03/2017"
          }
          """)
    ).andExpect(status().isForbidden())
    .andReturn();

  }

  @Test
  void testUpdateEntryWithAdminUser() throws Exception {
    User user = createUserTest("USER", "john@example.com");
    Entry entry = createTestEntry(user);

    MvcResult result = loginTestUser(createUserTest("ADMIN", "jane@example.com"));
    String token = result.getResponse().getCookie("jwt").getValue();

    mockMvc.perform(
      put("/entry/" + entry.getUuid())
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "description": "Update entry",
            "date": "09/03/2017"
          }
          """)
    ).andExpect(status().isOk())
    .andExpect(jsonPath("$.data.date").value("09/03/2017"))
    .andExpect(jsonPath("$.data.description").value("Update entry"));

  }

  @Test
  void testDeleteEntrySuccessfully() throws Exception {
    User user = createUserTest("USER", "john@example.com");
    Entry entry = createTestEntry(user);

    MvcResult result = loginTestUser(user);
    String token = result.getResponse().getCookie("jwt").getValue();

    mockMvc.perform(
      delete("/entry/" + entry.getUuid())
        .header("Authorization", "Bearer " + token)
    ).andExpect(status().isNoContent());

  }

  @Test
  void testDeleteEntryWithInvalidUser() throws Exception {
    User user = createUserTest("USER", "john@example.com");
    Entry entry = createTestEntry(user);

    MvcResult result = loginTestUser(createUserTest("USER", "jane@example.com"));
    String token = result.getResponse().getCookie("jwt").getValue();

    mockMvc.perform(
      delete("/entry/" + entry.getUuid())
        .header("Authorization", "Bearer " + token)
    ).andExpect(status().isForbidden());

  }

    @Test
    void testDeleteEntryWithoutExistingData() throws Exception {
    User user = createUserTest("USER", "john@example.com");

    MvcResult result = loginTestUser(user);
    String token = result.getResponse().getCookie("jwt").getValue();

    mockMvc.perform(
      delete("/entry/non-existent-uuid")
        .header("Authorization", "Bearer " + token)
    ).andExpect(status().isNotFound());

  }

  @Test
  void testDeleteEntryWithAdminUser() throws Exception {
    User user = createUserTest("USER", "john@example.com");
    Entry entry = createTestEntry(user);

    MvcResult result = loginTestUser(createUserTest("ADMIN", "jane@example.com"));
    String token = result.getResponse().getCookie("jwt").getValue();

    mockMvc.perform(
      delete("/entry/" + entry.getUuid())
        .header("Authorization", "Bearer " + token)
    ).andExpect(status().isNoContent());

  }



}