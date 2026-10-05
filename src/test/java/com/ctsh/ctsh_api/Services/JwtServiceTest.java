package com.ctsh.ctsh_api.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.ctsh.ctsh_api.Exceptions.ResourceNotFoundException;
import com.ctsh.ctsh_api.Models.User;
import com.ctsh.ctsh_api.Models.Enum.Role;
import com.ctsh.ctsh_api.Repositories.UserRepository;
import com.ctsh.ctsh_api.config.Auth.UserInfo;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private JwtService jwtService;

  private User mockUser(){
    User user = new User();
    user.setEmail("test@example.com");
    user.setRole(Role.USER);
    return user;
  }

  @BeforeEach
  void setUp() {
      ReflectionTestUtils.setField(jwtService, "secretKey", "mySecretKey1234567890123456789012345678901234567890123456789012345678901234567890");
      ReflectionTestUtils.setField(jwtService, "ttlSeconds", 3600L);
  }


  @Test
  void testGenerateTokenShouldReturnTokenWhenUserExists() {
    User mockUser = mockUser();
    when(userRepository.findByEmail("test@example.com")).thenReturn(java.util.Optional.of(mockUser));

    
    String token = jwtService.generateToken("test@example.com");
    assertNotNull(token);

    assertTrue(!token.isBlank());

    UserInfo userInfo = jwtService.decodeToken(token);

    assertNotNull(userInfo);

    assertEquals(mockUser.getEmail(), userInfo.getEmail());
    assertEquals(mockUser.getRole().name(), userInfo.getRole());
  }

  @Test
  void testDecodeTokenShouldNotReturnUserInfoWhenUserDoesNotExist() {
    User mockUser = mockUser();
    when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(java.util.Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> {
      jwtService.generateToken(mockUser.getEmail());
    });
  }

  @Test
  void testDecodeTokenWithWrongIssuerShouldThrowException() {
    String token = Jwts.builder()
        .subject("test@example.com")
        .issuer("wrong_issuer")
        .issuedAt(new Date())
        .expiration(
            new Date(System.currentTimeMillis() + 3600000)
        )
        .signWith(
            Keys.hmacShaKeyFor(
                "mySecretKey1234567890123456789012345678901234567890123456789012345678901234567890".getBytes(StandardCharsets.UTF_8)
            )
        )
        .compact();
    assertThrows(Exception.class, () -> {
      jwtService.decodeToken(token);
    });
  }

    @Test
  void testDecodeTokenWithWrongSignatureShouldThrowException() {
    String token = Jwts.builder()
        .subject("test@example.com")
        .issuer("ctsh_api")
        .issuedAt(new Date())
        .expiration(
            new Date(System.currentTimeMillis() + 3600000)
        )
        .signWith(
            Keys.hmacShaKeyFor(
                "mySecretKey123456789012345678901234567890123456789012345678901234567BAD_SIGNATURE".getBytes(StandardCharsets.UTF_8)
            )
        )
        .compact();
    assertThrows(Exception.class, () -> {
      jwtService.decodeToken(token);
    });
  }
}
