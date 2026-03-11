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
@Data // Use @Data to generate all Getters, Setters, and Required Methods
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Hero {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Builder.Default
  @Column(nullable = false)
  private int level = 1;

  @Builder.Default
  @Column(nullable = false)
  private int orderLevels = 0;

  @Builder.Default
  @Column(nullable = false)
  private int chaosLevels = 0;

  @Builder.Default
  @Column(nullable = false)
  private int warriorLevels = 0;

  @Builder.Default
  @Column(nullable = false)
  private int mageLevels = 0;

  @Column(name = "primary_class", nullable = true)
  @Enumerated(EnumType.STRING)
  private HeroClass primaryClass;

  @Column(name = "secondary_class", nullable = true)
  @Enumerated(EnumType.STRING)
  private HeroClass secondaryClass;

  @Builder.Default
  @Column(name = "is_hybrid", nullable = false)
  private boolean isHybrid = false;

  @Column(name = "hybrid_class", nullable = true)
  @Enumerated(EnumType.STRING)
  private HybridClass hybridClass;

  @Builder.Default
  @Column(nullable = false)
  private int health = 100;

  @Builder.Default
  @Column(name = "max_health", nullable = false)
  private int maxHealth = 100;

  @Builder.Default
  @Column(nullable = false)
  private int attack = 5;

  @Builder.Default
  @Column(nullable = false)
  private int defense = 5;

  @Builder.Default
  @Column(nullable = false)
  private int mana = 50;

  @Builder.Default
  @Column(name = "max_mana", nullable = false)
  private int maxMana = 50;

  @Builder.Default
  @Column(nullable = false)
  private int experience = 0;

  @Builder.Default
  @Column(name = "experience_to_next_level", nullable = false)
  private int experienceToNextLevel = 575;

  @ManyToOne
  @JoinColumn(name = "party_id")
  private Party party;
}
