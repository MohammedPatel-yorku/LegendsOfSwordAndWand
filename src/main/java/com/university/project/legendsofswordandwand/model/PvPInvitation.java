package com.university.project.legendsofswordandwand.model;

import com.university.project.legendsofswordandwand.model.enums.InvitationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

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
