package com.ctsh.ctsh_api.config.Auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PepperedPasswordEncoder implements PasswordEncoder {

  private final String pepper;
  private final PasswordEncoder delegate;

  public PepperedPasswordEncoder(String pepper) {
    this(pepper, new BCryptPasswordEncoder());
  }

  public PepperedPasswordEncoder(String pepper, PasswordEncoder delegate) {
    this.pepper = pepper;
    this.delegate = delegate;
  }

  private String applyPepper(String rawPassword) {
    return pepper + rawPassword;
  }

  @Override
  public String encode(CharSequence rawPassword) {
    return delegate.encode(applyPepper(rawPassword.toString()));
  }

  @Override
  public boolean matches(CharSequence rawPassword, String encodedPassword) {
    return delegate.matches(applyPepper(rawPassword.toString()), encodedPassword);
  }
}
