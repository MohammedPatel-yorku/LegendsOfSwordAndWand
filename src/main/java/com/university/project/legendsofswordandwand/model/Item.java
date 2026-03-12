package com.university.project.legendsofswordandwand.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

  public static Item bread() {
    return Item.builder().name("Bread").cost(200).hpRestore(20).build();
  }

  public static Item cheese() {
    return Item.builder().name("Cheese").cost(500).hpRestore(50).build();
  }

  public static Item steak() {
    return Item.builder().name("Steak").cost(1000).hpRestore(200).build();
  }

  public static Item water() {
    return Item.builder().name("Water").cost(150).manaRestore(10).build();
  }

  public static Item juice() {
    return Item.builder().name("Juice").cost(400).manaRestore(30).build();
  }

  public static Item wine() {
    return Item.builder().name("Wine").cost(750).manaRestore(100).build();
  }

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
