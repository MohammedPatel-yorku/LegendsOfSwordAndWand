package com.university.project.legendsofswordandwand.model;

import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "heroes")
@Getter
@NoArgsConstructor
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
  @Setter
  @JoinColumn(name = "owner_id")
  private User owner;

  @ManyToOne
  @JoinColumn(name = "party_id")
  @Setter
  private Party party;
}
