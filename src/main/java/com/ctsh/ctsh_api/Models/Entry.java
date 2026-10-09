package com.ctsh.ctsh_api.Models;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
public class Entry {
  @Id @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
  @Getter @Setter
  private String uuid;

  @ManyToOne
  @JoinColumn(name = "user_uuid")
  @Getter @Setter
  private User user;
  
  @Column( nullable = false, columnDefinition = "TEXT")
  @Getter @Setter
  private String description;

  @Column (nullable = false)
  @Getter @Setter
  private LocalDate date;

}