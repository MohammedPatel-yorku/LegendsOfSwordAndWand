package com.university.project.legendsofswordandwand.model;

import com.university.project.legendsofswordandwand.model.enums.InvitationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * JPA entity representing a PvP challenge invitation sent from one player to another.
 *
 * <p>An invitation starts in {@link InvitationStatus#PENDING} and transitions to {@link
 * InvitationStatus#ACCEPTED}, {@link InvitationStatus#REJECTED}, {@link InvitationStatus#EXPIRED},
 * or {@link InvitationStatus#CANCELED}. Both the sender and receiver must have at least one saved
 * party for the invitation to be valid.
 */
@Entity
@Table(name = "pvp_invitations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PvPInvitation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "sender_id", nullable = false)
  private User sender;

  @ManyToOne
  @JoinColumn(name = "receiver_id", nullable = false)
  private User receiver;

  @Enumerated(EnumType.STRING)
  @Setter
  private InvitationStatus status;

  private LocalDateTime createdAt;

  private LocalDateTime respondedAt;

  @Builder
  public PvPInvitation(
      User sender, User receiver, InvitationStatus status, LocalDateTime createdAt) {
    this.sender = sender;
    this.receiver = receiver;
    this.status = status;
    this.createdAt = createdAt;
  }
}
