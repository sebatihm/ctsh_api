package com.ctsh.ctsh_api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.ctsh.ctsh_api.Models.User;
import com.ctsh.ctsh_api.Models.Enum.Role;
import com.ctsh.ctsh_api.Repositories.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.containsString;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Value("${app.uploads.dir}")
  private String uploadsDir;

  @Value("${app.uploads.url-path}")
  private String uploadsUrlPath;

  private void testUserCreation(String role) {
    User user = new User();

    user.setName("John Doe");
    user.setEmail("john@example.com");
    user.setProfilePicture("profile.jpg");
    user.setPassword(passwordEncoder.encode("password123"));
    user.setRole( role == "ADMIN" ? Role.ADMIN : Role.USER);

    userRepository.save(user);
  }

  private MvcResult loginTestUser(String role) throws Exception {
    testUserCreation(role);
    
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
  void testLoginSuccessfully() throws Exception {
    testUserCreation("USER");

    String body = """
        {
            "email": "john@example.com",
            "password": "password123"
        }
        """;

    mockMvc.perform(
      post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body)
    )
    
    .andExpect(cookie().exists("jwt"))
    .andExpect(status().isOk());
  }

  @Test
  void testInvalidJwt() throws Exception {

      Cookie jwtCookie = new Cookie("jwt", "invalid-token");

      mockMvc.perform(
        get("/user")
          .cookie(jwtCookie)
      )
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.message")
          .value("Access denied: Invalid token or signature"));
  }


  @Test
  void testCreateUser() throws Exception {

    MockMultipartFile file = new MockMultipartFile(
      "profilePicture",
      "image.png",
      "image/png",
      "fake image content".getBytes()
    );

    mockMvc.perform(
      multipart("/user")
        .file(file)
        .param("name", "John Doe")
        .param("email", "john.doe@example.com")
        .param("password", "password123")
        )
    .andExpect(status().isCreated());
  }

  @Test
  void testCreateUserWithExistingEmail() throws Exception {

    testUserCreation("USER");

    MockMultipartFile file = new MockMultipartFile(
      "profilePicture",
      "image.png",
      "image/png",
      "fake image content".getBytes()
    );

    mockMvc.perform(
      multipart("/user")
        .file(file)
        .param("name", "Jane Doe")
        .param("email", "john@example.com")
        .param("password", "password123")
        )
    .andExpect(status().isConflict());
  }

  @Test
  void testRejectLoginWithWrongPassword() throws Exception {
    testUserCreation("USER");

    String body = """
        {
            "email": "john@example.com",
            "password": "wrongpassword"
        }
        """;

    mockMvc.perform(
      post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body)
    )
    .andExpect(status().isUnauthorized());
  }

  @Test
  void testRejectProtectedEndpointWithoutJwt() throws Exception {
    mockMvc.perform(
      get("/user")
    )
    .andExpect(status().isUnauthorized());
  }

  @Test
  void testAccessProtectedEndpointWithJwt() throws Exception {
    MvcResult result = loginTestUser("ADMIN");
    String jwt = result.getResponse().getCookie("jwt").getValue();
    mockMvc.perform(
      get("/user")
        .header("Authorization", "Bearer " + jwt)
    )
    .andExpect(status().isOk());
  }

  @Test
  void testAccessProtectedEndpointWithJwtWithoutAdminRole() throws Exception {
    MvcResult result = loginTestUser("USER");
    String jwt = result.getResponse().getCookie("jwt").getValue();
    mockMvc.perform(
      get("/user")
        .header("Authorization", "Bearer " + jwt)
    )
    .andExpect(status().isForbidden());
  }

  @Test
  void testLogoutSuccessfully() throws Exception {
    MvcResult result = loginTestUser("ADMIN");

    Cookie jwtCookie = result.getResponse().getCookie("jwt");

    MvcResult logoutResult = mockMvc.perform(
        post("/logout")
            .cookie(jwtCookie)
    )
    .andExpect(status().isOk())
    .andReturn();

    Cookie deletedCookie = logoutResult.getResponse().getCookie("jwt");

    assertNotNull(deletedCookie);
    assertEquals(0, deletedCookie.getMaxAge());
  }

  @Test
  void testFileExistenceBeforeAndAfterDeletion() throws Exception {

    MvcResult result = loginTestUser("ADMIN");

    MockMultipartFile file = new MockMultipartFile(
      "profilePicture",
      "image.jpg",
      "image/jpeg",
      "fake image content".getBytes()
    );

    mockMvc.perform(
      multipart("/user")
          .file(file)
          .param("name", "Jane Doe")
          .param("email", "jane@example.com")
          .param("password", "password123")
    )
    .andExpect(status().isCreated())
    .andExpect(jsonPath(
      "$.data.profilePicture",
      containsString(uploadsUrlPath)
    ));

    User user = userRepository
      .findByEmail("jane@example.com")
      .orElse(null);

    assertNotNull(user);

    Path picturePath = Paths.get(uploadsDir + "/profiles")
      .resolve(user.getProfilePicture());

    assertTrue(Files.exists(picturePath));

    mockMvc.perform(
      delete("/user/" + user.getUuid())
        .header("Authorization", "Bearer " + result.getResponse().getCookie("jwt").getValue())
    )
    .andExpect(status().isNoContent());

    assertTrue(Files.notExists(picturePath));
  }

}