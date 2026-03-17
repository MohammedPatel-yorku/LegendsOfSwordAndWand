package com.university.project.legendsofswordandwand.model;

import com.university.project.legendsofswordandwand.model.enums.RoomType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Campaign Entity class mapped to 'campaigns' Table with Lombok getters, builder, no-args and
 * all-args constructors. ID is automatically generated. Setters for all fields except ID.
 *
 * <p>A Campaign is owned by a User and is joined with a Party. It has a current room and an active
 * status.
 */
@Entity
@Table(name = "campaigns")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

  @Column(nullable = false)
  @Setter
  private int score = 0;

  @Enumerated(EnumType.STRING)
  @Column(name = "last_room_type", nullable = true)
  @Setter
  private RoomType lastRoomType;

  @Column(name = "room_pending", nullable = false)
  @Setter
  private boolean roomPending = false;

  @Builder
  public Campaign(User owner, Party party, int currentRoom, boolean active) {
    this.owner = owner;
    this.party = party;
    this.currentRoom = currentRoom;
    this.active = active;
    this.score = 0;
  }
}
