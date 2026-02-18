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
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String password;

  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Party> parties = new ArrayList<>();

  public User(String username, String password) {
    this.username = username;
    this.password = password;
  }
}
