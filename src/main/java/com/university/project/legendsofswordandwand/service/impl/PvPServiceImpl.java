package com.university.project.legendsofswordandwand.service.impl;

import com.university.project.legendsofswordandwand.model.PvPInvitation;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.model.enums.InvitationStatus;
import com.university.project.legendsofswordandwand.repository.PvPInvitationRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.IPvPService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PvPServiceImpl implements IPvPService {

  private final UserRepository userRepository;
  private final PvPInvitationRepository pvpInvitationRepository;

  @Override
  public void createInvitation(String senderUsername, String receiverUsername) {
    User sender =
        userRepository
            .findByUsername(senderUsername)
            .orElseThrow(() -> new RuntimeException("Sender not found"));
    User receiver =
        userRepository
            .findByUsername(receiverUsername)
            .orElseThrow(() -> new RuntimeException("Receiver not found"));

    if (sender.getParties().isEmpty())
      throw new RuntimeException("You must have at least one saved party to challenge");
    if (receiver.getParties().isEmpty())
      throw new RuntimeException("That player has no saved party");

    PvPInvitation invite =
        PvPInvitation.builder()
            .sender(sender)
            .receiver(receiver)
            .status(InvitationStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

    pvpInvitationRepository.save(invite);
  }

  @Override
  public void acceptInvitation(Long inviteId) {
    PvPInvitation invite =
        pvpInvitationRepository
            .findById(inviteId)
            .orElseThrow(() -> new RuntimeException("Invitation not found"));
    invite.setStatus(InvitationStatus.ACCEPTED);
    pvpInvitationRepository.save(invite);
  }
}
