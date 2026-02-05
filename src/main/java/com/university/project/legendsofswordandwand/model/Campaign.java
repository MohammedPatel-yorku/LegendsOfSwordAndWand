package com.university.project.legendsofswordandwand.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "campaigns")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Campaign {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne private User owner;

  private int currentRoom;
  private boolean active;

  public Campaign(User owner) {
    this.owner = owner;
    this.currentRoom = 1;
    this.active = true;
  }
}
