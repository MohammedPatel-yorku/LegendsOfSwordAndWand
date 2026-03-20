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

  @Column(nullable = false)
  @Setter
  private boolean saved = false;

  @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Hero> heroes = new ArrayList<>();

  @OneToOne(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
  private Inventory inventory;

  public int getCumulativeLevel() {
    return heroes.stream().filter(h -> !h.isTemporary()).mapToInt(Hero::getLevel).sum();
  }

  public int calculateScore() {
    int heroScore =
        heroes.stream().filter(h -> !h.isTemporary()).mapToInt(h -> h.getLevel() * 100).sum();
    int goldScore = gold * 10;
    return heroScore + goldScore;
  }

  @Builder
  public Party(User owner) {
    this.owner = owner;
    this.gold = 0;
    this.saved = false;
    this.heroes = new ArrayList<>();
  }
}
