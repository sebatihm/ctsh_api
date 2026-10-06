package com.ctsh.ctsh_api.Services;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;

import com.ctsh.ctsh_api.Dtos.UserResponseDto;
import com.ctsh.ctsh_api.Dtos.Validation.UserRequestDto;
import com.ctsh.ctsh_api.Exceptions.ResourceAlreadyExistsException;
import com.ctsh.ctsh_api.Exceptions.ResourceNotFoundException;
import com.ctsh.ctsh_api.Mappers.UserMapper;
import com.ctsh.ctsh_api.Models.User;
import com.ctsh.ctsh_api.Models.Enum.Role;
import com.ctsh.ctsh_api.Repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  
  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  private UserMapper userMapper;

  @Mock
  private FileService fileService;

  @InjectMocks
  private UserService userService;

  private User createTestUser() {
    User user = new User();
    user.setUuid("test-uuid");
    user.setName("Test User");
    user.setEmail("example123@email.com");
    user.setRole(Role.USER);
    user.setProfilePicture("profile.jpg");
    return user;
  }

  private void authenticateAs(String email, String role) {
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(
            email,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );

    SecurityContextHolder.getContext()
        .setAuthentication(authentication);
  }
  @BeforeEach
  void setUp() {
    userMapper = new UserMapper(fileService);
    userService = new UserService(
        userRepository,
        passwordEncoder,
        fileService,
        userMapper
    );
  }

  @AfterEach
  void tearDown() {
      SecurityContextHolder.clearContext();
  }


  private UserRequestDto createTestUserRequestDto() {
    MockMultipartFile file = new MockMultipartFile(
      "profilePicture",
      "profile.jpg",
      "image/jpeg",
      "fake image content".getBytes()
    );

    return new UserRequestDto(
      "Test User",
      "example123@email.com",
      "password",
      file
    );
  }

    private UserRequestDto updateTestUserRequestDto() {
    MockMultipartFile file = new MockMultipartFile(
      "profilePicture",
      "profile-copy.jpg",
      "image/jpeg",
      "fake image content".getBytes()
    );

    return new UserRequestDto(
      "Test User Updated",
      "example123@email.com",
      "password",
      file
    );
  }
  

  @Test
  void testUserCreation() {
    UserRequestDto dto = createTestUserRequestDto();


    when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
    when(userRepository.findByEmail("example123@email.com")).thenReturn(Optional.empty());
    when(fileService.saveFile(dto.profilePicture())).thenReturn("profile.jpg");
    when(userRepository.save(any(User.class)))
    .thenAnswer(invocation -> invocation.getArgument(0));
    when(fileService.getPublicUrl("profile.jpg")).thenReturn("profile.jpg");

    UserResponseDto result = userService.createUser(dto);

    assertEquals(dto.name(), result.getName());
    assertEquals(dto.email(), result.getEmail());
    assertEquals(Role.USER, result.getRole());
    assertEquals("profile.jpg", result.getProfilePicture());
  }

  @Test
  void testEmailAlreadyRegistered() {
    UserRequestDto dto = createTestUserRequestDto();
    when(userRepository.findByEmail("example123@email.com")).thenReturn(Optional.of(createTestUser()));

    assertThrows(ResourceAlreadyExistsException.class, () -> {
      userService.createUser(dto);
    });
  }

  @Test
  void testGetUserById() {
    authenticateAs("example123@email.com", "USER");

    User existingUser = createTestUser();
    when(userRepository.findById("test-uuid")).thenReturn(Optional.of(existingUser));
    when(fileService.getPublicUrl("profile.jpg")).thenReturn("profile.jpg");

    UserResponseDto result = userService.getUserById("test-uuid");

    assertEquals(existingUser.getName(), result.getName());
    assertEquals(existingUser.getEmail(), result.getEmail());
    assertEquals(existingUser.getRole(), result.getRole());
    assertEquals(existingUser.getProfilePicture(), result.getProfilePicture());
  }

  @Test
  void testGetUserByIdWithNotFound() {
    when(userRepository.findById("test-uuid")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> {
      userService.getUserById("test-uuid");
    });
  }

  @Test
  void testAdminGetUserById() {

    authenticateAs("admin@example.com", "ADMIN");

    User existingUser = createTestUser();
    when(userRepository.findById("test-uuid")).thenReturn(Optional.of(existingUser));
    when(fileService.getPublicUrl("profile.jpg")).thenReturn("profile.jpg");

    UserResponseDto result = userService.getUserById("test-uuid");

    assertEquals(existingUser.getName(), result.getName());
    assertEquals(existingUser.getEmail(), result.getEmail());
    assertEquals(existingUser.getRole(), result.getRole());
    assertEquals(existingUser.getProfilePicture(), result.getProfilePicture());
  }

  @Test
  void testUnauthorizedUserGetUserById() {

    authenticateAs("anotheruser@example.com", "USER");


    User existingUser = createTestUser();
    when(userRepository.findById("test-uuid")).thenReturn(Optional.of(existingUser));

    assertThrows(AccessDeniedException.class, () -> {
      userService.getUserById("test-uuid");
    });
  }

  @Test
  void testGetUsers() {
    when(userRepository.findAll()).thenReturn(List.of(createTestUser()));
    List<UserResponseDto> result = userService.getAllUsers();
    assertEquals(1, result.size());
    assertEquals("Test User", result.get(0).getName());
  }

  @Test
  void testUserUpdate() {
    authenticateAs("example123@email.com", "USER");


    UserRequestDto dto = updateTestUserRequestDto();
    User existingUser = createTestUser();

    when(userRepository.findById("test-uuid")).thenReturn(Optional.of(existingUser));
    when(fileService.updateFile(dto.profilePicture(), existingUser.getProfilePicture())).thenReturn("profile-copy.jpg");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(fileService.getPublicUrl("profile-copy.jpg")).thenReturn("profile-copy.jpg");
    UserResponseDto result = userService.updateUser("test-uuid", dto);

    assertEquals(dto.name(), result.getName());
    assertEquals("profile-copy.jpg", result.getProfilePicture());
  }

    @Test
  void testUserUpdateWithNotFound() {
    UserRequestDto dto = updateTestUserRequestDto();

    when(userRepository.findById("test-uuid")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> {
      userService.updateUser("test-uuid", dto);
    });
  }

  @Test
  void testAdminUpdate() {

    authenticateAs("admin@example.com", "ADMIN");

    UserRequestDto dto = updateTestUserRequestDto();
    User existingUser = createTestUser();

    when(userRepository.findById("test-uuid")).thenReturn(Optional.of(existingUser));
    when(fileService.updateFile(dto.profilePicture(), existingUser.getProfilePicture())).thenReturn("profile-copy.jpg");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(fileService.getPublicUrl("profile-copy.jpg")).thenReturn("profile-copy.jpg");
    UserResponseDto result = userService.updateUser("test-uuid", dto);

    assertEquals(dto.name(), result.getName());
    assertEquals("profile-copy.jpg", result.getProfilePicture());
  }

  @Test
  void testUnauthorizedUserUpdate() {

    authenticateAs("anotheruser@example.com", "USER");

    UserRequestDto dto = updateTestUserRequestDto();
    User existingUser = createTestUser();

    when(userRepository.findById("test-uuid")).thenReturn(Optional.of(existingUser));

    assertThrows(AccessDeniedException.class, () -> {
      userService.updateUser("test-uuid", dto);
    });
  }

  @Test 
  void testUserDeletion() {
    
    authenticateAs("example123@email.com", "USER");


    User existingUser = createTestUser();
    when(userRepository.findById("test-uuid")).thenReturn(Optional.of(existingUser));
    
    userService.deleteUser("test-uuid");
    verify(userRepository).delete(existingUser);
  }

  @Test
  void testAdminDelete() {
    authenticateAs("admin@example.com", "ADMIN");

    User existingUser = createTestUser();
    when(userRepository.findById("test-uuid")).thenReturn(Optional.of(existingUser));
    
    userService.deleteUser("test-uuid");
    verify(userRepository).delete(existingUser);
    
  }

  @Test
  void testUnauthorizedUserDeletion() {
    authenticateAs("anotheruser@example.com", "USER");

    User existingUser = createTestUser();
    when(userRepository.findById("test-uuid")).thenReturn(Optional.of(existingUser));

    assertThrows(AccessDeniedException.class, () -> {
      userService.deleteUser("test-uuid");
    });

    verify(userRepository, never()).delete(any(User.class));
  }

  @Test
  void testLoadUserByUsernameShouldReturnUserDetails() {
    User user = createTestUser();

    when(userRepository.findByEmail(user.getEmail()))
        .thenReturn(Optional.of(user));

    UserDetails result = userService.loadUserByUsername(user.getEmail());

    assertEquals(user.getEmail(), result.getUsername());
    assertEquals(user.getPassword(), result.getPassword());
    assertTrue(result.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

    verify(userRepository).findByEmail(user.getEmail());
  }

  @Test
  void testLoadUserByUsernameShouldThrowExceptionWhenUserDoesNotExist() {
    when(userRepository.findByEmail("unknown@example.com"))
        .thenReturn(Optional.empty());

    UsernameNotFoundException exception = assertThrows(
        UsernameNotFoundException.class,
        () -> userService.loadUserByUsername("unknown@example.com")
    );

    assertEquals(
        "User not found with email: unknown@example.com",
        exception.getMessage()
    );

    verify(userRepository).findByEmail("unknown@example.com");
  }

}
