package com.ctsh.ctsh_api.Controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ctsh.ctsh_api.Dtos.UserResponseDto;
import com.ctsh.ctsh_api.Dtos.Validation.UserRequestDto;
import com.ctsh.ctsh_api.Exceptions.ResourceNotFoundException;
import com.ctsh.ctsh_api.Models.Enum.Role;
import com.ctsh.ctsh_api.Services.UserService;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService userService;

  private UserResponseDto user() {
    UserResponseDto user = new UserResponseDto();
    user.setUuid("test-uuid");
    user.setName("John Doe");
    user.setRole(Role.USER);
    user.setEmail("john.doe@example.com");
    return user;
  }

  @Test
  void testGetAllUsers() throws Exception {
    when(userService.getAllUsers()).thenReturn(List.of(user()));

    mockMvc.perform(get("/user"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[0].uuid").value("test-uuid"))
      .andExpect(jsonPath("$.data[0].name").value("John Doe"))
      .andExpect(jsonPath("$.data[0].role").value("USER"))
      .andExpect(jsonPath("$.data[0].email").value("john.doe@example.com"));
    
    verify(userService).getAllUsers();
  }

  @Test
  void testGetUserById() throws Exception {
    when(userService.getUserById("test-uuid")).thenReturn(user());

    mockMvc.perform(get("/user/test-uuid"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.uuid").value("test-uuid"))
      .andExpect(jsonPath("$.data.name").value("John Doe"))
      .andExpect(jsonPath("$.data.role").value("USER"))
      .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));

    verify(userService).getUserById("test-uuid");
  }

  @Test
  void testGetUserByIdWithoutData() throws Exception {
    when(userService.getUserById("test-uuid")).thenThrow(new ResourceNotFoundException("User not found"));

    mockMvc.perform(get("/user/test-uuid"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(userService).getUserById("test-uuid");
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

    ArgumentCaptor<UserRequestDto> captor =
        ArgumentCaptor.forClass(UserRequestDto.class);

    verify(userService).createUser(captor.capture());
    UserRequestDto request = captor.getValue();

    assertNotNull(request.profilePicture());
    assertEquals("image.png", request.profilePicture().getOriginalFilename());
    assertEquals("image/png", request.profilePicture().getContentType());
  }

  @Test
  void testCreateUserWithoutProfilePicture() throws Exception {

    mockMvc.perform(
      multipart("/user")
        .param("name", "John Doe")
        .param("email", "john.doe@example.com")
        .param("password", "password123")
        )
    .andExpect(status().isBadRequest());
  }

  @Test
  void testCreateUserWithoutName() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "profilePicture",
        "image.png",
        "image/png",
        "fake image content".getBytes()
    );
    mockMvc.perform(
      multipart("/user")
        .file(file)
        .param("email", "john.doe@example.com")
        .param("password", "password123")
        )
    .andExpect(status().isBadRequest());
  }

  @Test
  void testCreateUserWithoutEmail() throws Exception {
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
        .param("password", "password123")
        )
    .andExpect(status().isBadRequest());
  }

  @Test
  void testCreateUserWithoutValidEmail() throws Exception {
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
        .param("email", "invalid-email")
        .param("password", "password123")
        )
    .andExpect(status().isBadRequest());
  }

  @Test
  void testCreateUserWithoutPassword() throws Exception {
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
        )
    .andExpect(status().isBadRequest());
  }

  @Test
  void testCreateUserWithoutValidPassword() throws Exception {
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
        .param("password", "short")
        )
    .andExpect(status().isBadRequest());
  }

  @Test 
  void testUpdateUser() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "profilePicture",
        "image.png",
        "image/png",
        "fake image content".getBytes()
    );

    mockMvc.perform(
      multipart("/user/test-uuid")
        .file(file)
        .param("name", "John Doe Updated")
        .with(request -> {
            request.setMethod("PUT");
            return request;
        })
      )
    .andExpect(status().isOk());
  }


  @Test
  void testDeleteUser() throws Exception {
    mockMvc.perform(
      post("/user/test-uuid")
        .with(request -> {
            request.setMethod("DELETE");
            return request;
        })
      )
    .andExpect(status().isNoContent());
  }
}
