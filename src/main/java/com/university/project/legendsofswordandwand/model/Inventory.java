package com.university.project.legendsofswordandwand.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

/**
 * Inventory Entity class mapped to 'inventories' Table with Lombok getters, builder, no-args and
 * all-args constructors. ID is automatically generated.
 *
 * <p>An Inventory belongs to a Party and stores a list of item IDs.
 */
@Entity
@Table(name = "inventories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @Setter
  @JoinColumn(name = "party_id")
  private Party party;

  @ElementCollection private List<Long> itemIds = new ArrayList<>();

  @Builder
  public Inventory(Party party) {
    this.party = party;
    this.itemIds = new ArrayList<>();
  }
}
