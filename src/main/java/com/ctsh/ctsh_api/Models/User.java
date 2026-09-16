package com.ctsh.ctsh_api.Models;

import com.ctsh.ctsh_api.Models.Enum.Role;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@NoArgsConstructor
public class User {
  @Id @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
  @Getter @Setter
  private String uuid;

  @Getter @Setter 
  private String name;

  @Getter @Setter
  private String email;
  
  @Getter @Setter
  private String password;

  @Enumerated(EnumType.STRING)
  @Getter @Setter
  private Role role;

  @Getter @Setter
  private String profilePicture;

  public User(String uuid, String name, String email, String password, Role role, String profilePicture) {
    this.uuid = uuid;
    this.name = name;
    this.email = email;
    this.password = password;
    this.role = role;
    this.profilePicture = profilePicture;
  }

}