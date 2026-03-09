package com.university.project.legendsofswordandwand.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

/**
 * Party Entity class mapped to 'parties' Table with Lombok getters, builder, no-args and all-args
 * constructors. ID is automatically generated. Setter for owner field.
 *
 * <p>A Party is owned by a User and itself owns a List of Hero Objects.
 */
@Entity
@Table(name = "parties")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Party {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @Setter
  @JoinColumn(name = "owner_id")
  private User owner;

  @Column(nullable = false)
  @Setter
  private int gold = 0;

  @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Hero> heroes = new ArrayList<>();

  @Builder
  public Party(User owner, Party party) {
    this.owner = owner;
    this.heroes = party.heroes;
  }

  public int getCumulativeLevel() {
    return heroes.stream().mapToInt(Hero::getLevel).sum();
  }
}
