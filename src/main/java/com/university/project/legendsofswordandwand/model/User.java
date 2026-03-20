package com.university.project.legendsofswordandwand.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

/**
 * User Entity class mapped to 'users' Table with Lombok getters and a no-args constructor. ID is
 * automatically generated.
 *
 * <p>A User owns a List of Party Objects. It has a username and password.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  @Setter
  private String username;

  @Column(nullable = false)
  @Setter
  private String password;

  @Column(nullable = false)
  @Setter
  private int pvpWins = 0;

  @Column(nullable = false)
  @Setter
  private int pvpLosses = 0;

  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Party> parties = new ArrayList<>();

  @Builder
  public User(String username, String password) {
    this.username = username;
    this.password = password;
    this.pvpWins = 0;
    this.pvpLosses = 0;
  }
}
