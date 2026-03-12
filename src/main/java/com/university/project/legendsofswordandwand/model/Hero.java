package com.university.project.legendsofswordandwand.model;

import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;
import jakarta.persistence.*;
import lombok.*;

/**
 * Hero Entity class mapped to 'heroes' Table with Lombok getters, builder, no-args and all-args
 * constructors. ID is automatically generated. Setters for all fields except ID.
 *
 * <p>A Hero
 */
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

  @Enumerated(EnumType.STRING)
  @Column(name = "starting_class", nullable = false)
  private HeroClass startingClass;

  @Column(nullable = false)
  @Setter
  private int level = 1;

  @Column(nullable = false)
  @Setter
  private int orderLevels = 0;

  @Column(nullable = false)
  @Setter
  private int chaosLevels = 0;

  @Column(nullable = false)
  @Setter
  private int warriorLevels = 0;

  @Column(nullable = false)
  @Setter
  private int mageLevels = 0;

  @Enumerated(EnumType.STRING)
  @Column(name = "primary_class", nullable = true)
  @Setter
  private HeroClass primaryClass;

  @Enumerated(EnumType.STRING)
  @Column(name = "secondary_class", nullable = true)
  @Setter
  private HeroClass secondaryClass;

  @Column(name = "is_hybrid", nullable = false)
  @Setter
  private boolean isHybrid = false;

  @Enumerated(EnumType.STRING)
  @Column(name = "hybrid_class", nullable = true)
  @Setter
  private HybridClass hybridClass;

  @Column(nullable = false)
  @Setter
  private int health = 100;

  @Column(name = "max_health", nullable = false)
  @Setter
  private int maxHealth = 100;

  @Column(nullable = false)
  @Setter
  private int attack = 5;

  @Column(nullable = false)
  @Setter
  private int defense = 5;

  @Column(nullable = false)
  @Setter
  private int mana = 50;

  @Column(name = "max_mana", nullable = false)
  @Setter
  private int maxMana = 50;

  @Column(nullable = false)
  @Setter
  private int experience = 0;

  @Column(name = "experience_to_next_level", nullable = false)
  @Setter
  private int experienceToNextLevel = 575;

  @ManyToOne
  @JoinColumn(name = "party_id")
  private Party party;

  @Builder
  public Hero(String name, HeroClass startingClass, Party party) {
    this.name = name;
    this.startingClass = startingClass;
    this.party = party;
  }
}
