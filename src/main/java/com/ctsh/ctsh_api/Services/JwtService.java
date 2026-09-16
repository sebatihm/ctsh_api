package com.ctsh.ctsh_api.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ctsh.ctsh_api.Models.User;
import com.ctsh.ctsh_api.Repositories.UserRepository;
import com.ctsh.ctsh_api.config.UserInfo;

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
    userInfo.setEmail(user.getEmail());
    userInfo.setRole(user.getRole().name());
    userInfo.setExp(System.currentTimeMillis() + 3600000); 
    userInfo.setIat(System.currentTimeMillis());

    return Jwts.builder()
      .claims(userInfo.toMap())
      .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(secretKey.getBytes()))
      .compact();
  }

  public UserInfo decodeToken(String token) {
      Claims claims = Jwts.parser()
        .verifyWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(secretKey.getBytes()))
        .build()
        .parseSignedClaims(token)
        .getPayload();

      UserInfo info = new UserInfo();
      info.setEmail(claims.get("email", String.class));
      info.setRole(claims.get("role", String.class));
      info.setExp(claims.getExpiration().getTime());
      info.setIat(claims.getIssuedAt().getTime());
      return info;
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parser()
        .verifyWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(secretKey.getBytes()))
        .build()
        .parseSignedClaims(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
