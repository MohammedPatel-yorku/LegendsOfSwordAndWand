package com.university.project.legendsofswordandwand.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "heroes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hero {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String heroClass;

  @Column(nullable = false)
  private int level;

  @Column(nullable = false)
  @Setter
  private int health;

  @Column(nullable = false)
  private int attack;

  @ManyToOne private User owner;

  public Hero(String name, String heroClass, int level, int health, int attack, User owner) {
    this.name = name;
    this.heroClass = heroClass;
    this.level = level;
    this.health = health;
    this.attack = attack;
    this.owner = owner;
  }
}
