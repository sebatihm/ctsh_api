package com.ctsh.ctsh_api.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
public class Mood {
  @Id @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
  @Getter @Setter
  private String uuid;

  @Column (nullable = false, unique = true, length = 50)
  @Getter @Setter
  private String name;

  @Column (nullable = false)
  @Getter @Setter
  private int count;
}
