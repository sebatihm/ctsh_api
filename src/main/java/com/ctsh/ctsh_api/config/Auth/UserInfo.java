package com.ctsh.ctsh_api.config.Auth;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

public class UserInfo {
  @Getter @Setter 
  private String email;
  
  @Getter @Setter 
  private String role;



  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("role", role);
    return map;
  }
}
