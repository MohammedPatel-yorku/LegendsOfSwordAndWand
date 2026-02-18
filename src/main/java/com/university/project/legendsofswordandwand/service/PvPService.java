package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.PvPInvitation;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.model.enums.InvitationStatus;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import com.university.project.legendsofswordandwand.repository.PvPInvitationRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PvPService {

  private final UserRepository userRepository;
  private final PartyRepository partyRepository;
  private final PvPInvitationRepository pvPInvitationRepository;

  public void createInvitation(Long userId, String enemyUsername) {

    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not Found"));
    User enemy =
        userRepository
            .findByUsername(enemyUsername)
            .orElseThrow(() -> new RuntimeException("User Not Found"));

    if (!partyRepository.existsById(userId))
      throw new RuntimeException("Must have at least one saved party");

    if (!partyRepository.existsById(enemy.getId()))
      throw new RuntimeException("Enemy has no saved party");

    PvPInvitation invite =
        PvPInvitation.builder()
            .sender(user)
            .receiver(enemy)
            .status(InvitationStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

    pvPInvitationRepository.save(invite);
  }

  public void acceptInvitation(Long inviteId) {

    PvPInvitation invite =
        pvPInvitationRepository
            .findById(inviteId)
            .orElseThrow(() -> new RuntimeException("Invitation Not Found"));

    invite.setStatus(InvitationStatus.ACCEPTED);
    pvPInvitationRepository.save(invite);
  }
}
