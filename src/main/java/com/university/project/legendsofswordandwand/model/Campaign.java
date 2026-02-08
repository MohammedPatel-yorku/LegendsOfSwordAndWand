package com.university.project.legendsofswordandwand.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "campaigns")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Campaign {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @Setter
  @JoinColumn(name = "owner_id")
  private User owner;

  @OneToOne
  @JoinColumn(name = "party_id", nullable = false, unique = true)
  @Setter
  private Party party;

  @Column(nullable = false)
  @Setter
  private int currentRoom;

  @Column(nullable = false)
  @Setter
  private boolean active;
}
