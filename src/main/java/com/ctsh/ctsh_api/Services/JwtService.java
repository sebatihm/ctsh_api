package com.ctsh.ctsh_api.Services;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ctsh.ctsh_api.Models.User;
import com.ctsh.ctsh_api.Repositories.UserRepository;
import com.ctsh.ctsh_api.config.Auth.UserInfo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Service 
public class JwtService {
  
  private final UserRepository userRepository;

  @Value ("${app.jwt.secret}")
  private String secretKey;
  

  public JwtService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }
  
  public String generateToken(String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

    UserInfo userInfo = new UserInfo();
    userInfo.setRole(user.getRole().name());

    return Jwts.builder()
      .claims(userInfo.toMap())
      .issuer("ctsh_api")
      .issuedAt(new Date(System.currentTimeMillis()))
      .subject(user.getEmail())
      .expiration(new Date(System.currentTimeMillis() + 3600000))
      .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
      .compact();
  }

  public UserInfo decodeToken(String token) {
      Claims claims = Jwts.parser()
        .verifyWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
        .requireIssuer("ctsh_api")
        .build()
        .parseSignedClaims(token)
        .getPayload();

      UserInfo info = new UserInfo();
      info.setEmail(claims.getSubject());
      info.setRole(claims.get("role", String.class));
      return info;
  }

}
