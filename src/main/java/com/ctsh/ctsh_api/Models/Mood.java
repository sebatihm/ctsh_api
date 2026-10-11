package com.ctsh.ctsh_api.Models;

import org.hibernate.annotations.Collate;

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

  @Collate("utf8mb4_bin")
  @Column (nullable = false, unique = true, length = 50)
  @Getter @Setter
  private String name;

  @Column (nullable = false)
  @Getter @Setter
  private int count;
}
