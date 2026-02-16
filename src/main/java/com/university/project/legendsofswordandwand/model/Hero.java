package com.university.project.legendsofswordandwand.model;

import com.university.project.legendsofswordandwand.model.enums.HeroClass;
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

    @Column(name = "hero_class", nullable = false)
    @Enumerated(EnumType.STRING)
    private HeroClass heroClass;

    @Builder.Default
    @Column(nullable = false)
    private int level = 1;

    @Builder.Default
    @Column(nullable = false)
    private int health = 100;

    @Builder.Default
    @Column(nullable = false)
    private int attack = 5;

    @Builder.Default
    @Column(nullable = false)
    private int defense = 5;

    @Builder.Default
    @Column(nullable = false)
    private int mana = 50;

    @ManyToOne
    @JoinColumn(name = "party_id")
    private Party party;
}