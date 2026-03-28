package com.university.project.legendsofswordandwand.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing a purchasable consumable item available in the inn shop. Items can
 * restore HP, restore mana, or revive a fallen hero (Elixir).
 *
 * <p>Static factory methods correspond exactly to the items defined in the game specification.
 */
@Entity
@Table(name = "items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(nullable = false)
  private int cost;

  @Column(name = "hp_restore", nullable = false)
  private int hpRestore;

  @Column(name = "mana_restore", nullable = false)
  private int manaRestore;

  @Column(name = "revives", nullable = false)
  private boolean revives;

  @Builder
  public Item(String name, int cost, int hpRestore, int manaRestore, boolean revives) {
    this.name = name;
    this.cost = cost;
    this.hpRestore = hpRestore;
    this.manaRestore = manaRestore;
    this.revives = revives;
  }

  /**
   * Creates a Bread item: costs 200g, restores 20 HP.
   *
   * @return a new {@link Item} configured as Bread
   */
  public static Item bread() {
    return Item.builder().name("Bread").cost(200).hpRestore(20).build();
  }

  /**
   * Creates a Cheese item: costs 500g, restores 50 HP.
   *
   * @return a new {@link Item} configured as Cheese
   */
  public static Item cheese() {
    return Item.builder().name("Cheese").cost(500).hpRestore(50).build();
  }

  /**
   * Creates a Steak item: costs 1000g, restores 200 HP.
   *
   * @return a new {@link Item} configured as Steak
   */
  public static Item steak() {
    return Item.builder().name("Steak").cost(1000).hpRestore(200).build();
  }

  /**
   * Creates a Water item: costs 150g, restores 10 mana.
   *
   * @return a new {@link Item} configured as Water
   */
  public static Item water() {
    return Item.builder().name("Water").cost(150).manaRestore(10).build();
  }

  /**
   * Creates a Juice item: costs 400g, restores 30 mana.
   *
   * @return a new {@link Item} configured as Juice
   */
  public static Item juice() {
    return Item.builder().name("Juice").cost(400).manaRestore(30).build();
  }

  /**
   * Creates a Wine item: costs 750g, restores 100 mana.
   *
   * @return a new {@link Item} configured as Wine
   */
  public static Item wine() {
    return Item.builder().name("Wine").cost(750).manaRestore(100).build();
  }

  /**
   * Creates an Elixir item: costs 2000g, revives the hero and fully restores HP and mana.
   *
   * @return a new {@link Item} configured as Elixir
   */
  public static Item elixir() {
    return Item.builder()
        .name("Elixir")
        .cost(2000)
        .revives(true)
        .hpRestore(Integer.MAX_VALUE)
        .manaRestore(Integer.MAX_VALUE)
        .build();
  }
}
