package com.ctsh.ctsh_api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.ctsh.ctsh_api.Models.Mail;
import com.ctsh.ctsh_api.Models.User;
import com.ctsh.ctsh_api.Models.Enum.Role;
import com.ctsh.ctsh_api.Repositories.MailRepository;
import com.ctsh.ctsh_api.Repositories.UserRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MailIntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private MailRepository mailRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

    private User createUserTest(String role, String email) {
    User user = new User();

    user.setName("Test User");
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

  private Mail createTestMail(User from, User to, String message) {
    Mail mail = new Mail();
    mail.setMessage(message);
    mail.setFrom(from);
    mail.setTo(to);
    mailRepository.save(mail);
    return mail;
  }

  private MvcResult login() throws Exception {
    User testUser = createUserTest("USER", "test@example.com");
    return loginTestUser(testUser);
  }

  @Test
  void testGetMails() throws Exception {
    User testFromUser = createUserTest("USER", "from@example.com");
    User testToUser = createUserTest("USER", "to@example.com");
    createTestMail(testFromUser, testToUser, "Test Body 1");
    createTestMail(testFromUser, testToUser, "Test Body 2");
    createTestMail(testFromUser, testToUser, "Test Body 3");
    
    mockMvc.perform(
      get("/mail")
        .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isOk())
    .andExpect(jsonPath("$.data[0].message").value("Test Body 1"))
    .andExpect(jsonPath("$.data[1].message").value("Test Body 2"))
    .andExpect(jsonPath("$.data[2].message").value("Test Body 3"))
    .andReturn();
  }

  @Test
  void testGetMail() throws Exception {
    User testFromUser = createUserTest("USER", "from@example.com");
    User testToUser = createUserTest("USER", "to@example.com");
    Mail testMail = createTestMail(testFromUser, testToUser, "Test Body 1");

    
    mockMvc.perform(
      get("/mail/{id}", testMail.getUuid())
        .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isOk())
    .andExpect(jsonPath("$.data.message").value("Test Body 1"))
    .andReturn();
  }

  @Test
  void testGetNonexistentMail() throws Exception {
    
    mockMvc.perform(
      get("/mail/nonexistent-uuid")
        .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isNotFound())
    .andReturn();
  }

  @Test
  void testCreateMail() throws Exception {
    MvcResult result = login();
    User testFromUser = createUserTest("USER", "from@example.com");
    User testToUser = createUserTest("USER", "to@example.com");
    
    String body = """
    {
        "message": "Test Body 1",
        "fromUuid": "%s",
        "toUuid": "%s"
    }
    """.formatted(testFromUser.getUuid(), testToUser.getUuid());


    mockMvc.perform(
      post("/mail")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body)
        .cookie(result.getResponse().getCookie("jwt"))
    ).andExpect(status().isCreated())
    .andExpect(jsonPath("$.data.message").value("Test Body 1"))
    .andExpect(jsonPath("$.data.from.uuid").value(testFromUser.getUuid()))
    .andExpect(jsonPath("$.data.to.uuid").value(testToUser.getUuid()))
    .andReturn();
  }

  @Test
  void testCreateMailWithoutAuthentication() throws Exception {
    
    String body = """
    {
        "message": "Test Body 1",
        "fromUuid": "%s",
        "toUuid": "%s"
    }
    """.formatted("example-from-uuid", "example-to-uuid");


    mockMvc.perform(
      post("/mail")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body)
    ).andExpect(status().isUnauthorized())
    .andReturn();
  }

  @Test
  void testCreateMailWithNonexistentFromUser() throws Exception {
    MvcResult result = login();
    User testFromUser = createUserTest("USER", "from@example.com");
    
    String body = """
    {
        "message": "Test Body 1",
        "fromUuid": "NONEXISTENT_UUID",
        "toUuid": "%s"
    }
    """.formatted(testFromUser.getUuid());


    mockMvc.perform(
      post("/mail")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body)
        .cookie(result.getResponse().getCookie("jwt"))
    ).andExpect(status().isNotFound())
    .andReturn();
  }

  @Test
  void testCreateMailWithInvalidData() throws Exception {
    MvcResult result = login();
    User testFromUser = createUserTest("USER", "from@example.com");
    User testToUser = createUserTest("USER", "to@example.com");
    
    String body = """
    {
        "fromUuid": "%s",
        "toUuid": "%s"
    }
    """.formatted(testFromUser.getUuid(), testToUser.getUuid());


    mockMvc.perform(
      post("/mail")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body)
        .cookie(result.getResponse().getCookie("jwt"))
    ).andExpect(status().isBadRequest())
    .andReturn();
  }

  @Test
  void testUpdateMail() throws Exception {
    MvcResult result = login();
    User testFromUser = createUserTest("USER", "from@example.com");
    User testToUser = createUserTest("USER", "to@example.com");

    Mail testMail = createTestMail(testFromUser, testToUser, "Test Body 1");

    User testUpdateFromUser = createUserTest("USER", "update-from@example.com");
    User testUpdateToUser = createUserTest("USER", "update-to@example.com");
    
    String body = """
    {
        "fromUuid": "%s",
        "toUuid": "%s"
    }
    """.formatted(testUpdateFromUser.getUuid(), testUpdateToUser.getUuid());


    mockMvc.perform(
      put("/mail/" + testMail.getUuid())
        .contentType(MediaType.APPLICATION_JSON)
        .content(body)
        .cookie(result.getResponse().getCookie("jwt"))
    ).andExpect(status().isOk())
    .andExpect(jsonPath("$.data.message").value("Test Body 1"))
    .andExpect(jsonPath("$.data.from.email").value(testUpdateFromUser.getEmail()))
    .andExpect(jsonPath("$.data.to.email").value(testUpdateToUser.getEmail()))
    .andReturn();
  }

  @Test
  void testUpdateMailWithoutAuthentication() throws Exception {
    
    String body = """
    {
        "fromUuid": "%s",
        "toUuid": "%s"
    }
    """.formatted("existent_uuid", "existent_uuid");


    mockMvc.perform(
      put("/mail/existent_uuid")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body)
    ).andExpect(status().isUnauthorized())
    .andReturn();
  }

  @Test
  void testUpdateNonExistentMail() throws Exception {
    MvcResult result = login();

    User testUpdateFromUser = createUserTest("USER", "update-from@example.com");
    User testUpdateToUser = createUserTest("USER", "update-to@example.com");
    
    String body = """
    {
        "fromUuid": "%s",
        "toUuid": "%s"
    }
    """.formatted(testUpdateFromUser.getUuid(), testUpdateToUser.getUuid());


    mockMvc.perform(
      put("/mail/NONEXISTENT_UUID")
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(result.getResponse().getCookie("jwt"))
        .content(body)
    ).andExpect(status().isNotFound())
    .andReturn();
  }

  @Test
  void testUpdateMailWithNonExistentUser() throws Exception {
    MvcResult result = login();
    User testFromUser = createUserTest("USER", "from@example.com");
    User testToUser = createUserTest("USER", "to@example.com");

    Mail testMail = createTestMail(testFromUser, testToUser, "Test Body 1");

    User testUpdateFromUser = createUserTest("USER", "update-from@example.com");
    
    String body = """
    {
        "fromUuid": "%s",
        "toUuid": "%s"
    }
    """.formatted(testUpdateFromUser.getUuid(), "NONEXISTENT_UUID");


    mockMvc.perform(
      put("/mail/" + testMail.getUuid())
        .contentType(MediaType.APPLICATION_JSON)
        .content(body)
        .cookie(result.getResponse().getCookie("jwt"))
    ).andExpect(status().isNotFound())
    .andReturn();
  }

  @Test
  void testDeleteMail() throws Exception {
    MvcResult result = login();
    User testFromUser = createUserTest("USER", "from@example.com");
    User testToUser = createUserTest("USER", "to@example.com");

    Mail testMail = createTestMail(testFromUser, testToUser, "Test Body 1");

    mockMvc.perform(
      delete("/mail/" + testMail.getUuid())
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(result.getResponse().getCookie("jwt"))
    ).andExpect(status().isNoContent())
    .andReturn();
  }

  @Test
  void testDeleteMailWithoutAuthentication() throws Exception {

    mockMvc.perform(
      delete("/mail/existent_uuid")
        .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isUnauthorized())
    .andReturn();
  }

  @Test
  void testDeleteNonExistentMail() throws Exception {
    MvcResult result = login();
    mockMvc.perform(
      delete("/mail/NONEXISTENT_UUID")
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(result.getResponse().getCookie("jwt"))
    ).andExpect(status().isNotFound())
    .andReturn();
  }
}