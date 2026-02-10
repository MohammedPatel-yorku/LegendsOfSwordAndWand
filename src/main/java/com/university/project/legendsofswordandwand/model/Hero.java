package com.university.project.legendsofswordandwand.model;

import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import jakarta.persistence.*;
import lombok.*;

/**
 * Hero Entity class mapped to 'heroes' Table with Lombok getters, builder, no-args and all-args
 * constructors. ID is automatically generated. Setters for all fields except ID.
 *
 * <p>A Hero owned by a Party. It has a name, HeroClass, level, health and attack stats.
 */
@Entity
@Table(name = "heroes")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Hero {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  @Setter
  private String name;

  @Column(name = "hero_class", nullable = false)
  @Setter
  @Enumerated(EnumType.STRING)
  private HeroClass heroClass;

  @Column(nullable = false)
  @Setter
  private int level;

  @Column(nullable = false)
  @Setter
  private int health;

  @Column(nullable = false)
  @Setter
  private int attack;

  @ManyToOne
  @JoinColumn(name = "party_id")
  @Setter
  private Party party;
}
