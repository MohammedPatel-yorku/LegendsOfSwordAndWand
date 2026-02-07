package com.university.project.legendsofswordandwand.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "parties")
@Getter
@NoArgsConstructor
public class Party {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne @Setter @JoinColumn(name = "owner_id") private User owner;

  @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Hero> heroes = new ArrayList<>();

  public void addHero(Hero hero) {
    heroes.add(hero);
  }
}
