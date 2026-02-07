package com.university.project.legendsofswordandwand.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "campaigns")
@Getter
@NoArgsConstructor
public class Campaign {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne @Setter @JoinColumn(name = "owner_id") private User owner;

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
