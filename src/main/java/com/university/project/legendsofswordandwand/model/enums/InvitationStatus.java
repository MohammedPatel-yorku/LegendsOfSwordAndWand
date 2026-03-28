package com.university.project.legendsofswordandwand.model.enums;

import com.university.project.legendsofswordandwand.model.PvPInvitation;

/** Represents the lifecycle status of a {@link PvPInvitation}. */
public enum InvitationStatus {
  /** The invitation has been sent and is awaiting a response from the receiver. */
  PENDING,
  /** The receiver has accepted the invitation; the battle may proceed. */
  ACCEPTED,
  /** The receiver has declined the invitation. */
  REJECTED,
  /** The invitation was not responded to within the allowed time window. */
  EXPIRED,
  /** The sender withdrew the invitation before a response was given. */
  CANCELED
}
