package com.ctsh.ctsh_api.config;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

public class UserInfo {
  @Getter @Setter 
  private String email;
  
  @Getter @Setter 
  private String role;

  @Getter @Setter 
  private long exp;

  @Getter @Setter
  private long iat;


  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("email", email);
    map.put("role", role);
    map.put("exp", exp);
    map.put("iat", iat);
    return map;
  }
}
