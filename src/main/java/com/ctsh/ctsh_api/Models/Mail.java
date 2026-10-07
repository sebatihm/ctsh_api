package com.ctsh.ctsh_api.Models;

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
public class Mail {
  @Id @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
  @Getter @Setter
  private String uuid;

  @ManyToOne
  @JoinColumn(name = "from_uuid")
  @Getter @Setter
  private User from;

  @ManyToOne
  @JoinColumn(name = "to_uuid")
  @Getter @Setter
  private User to;

  @Column (nullable = false, columnDefinition = "TEXT")
  @Getter @Setter
  private String message;
}
