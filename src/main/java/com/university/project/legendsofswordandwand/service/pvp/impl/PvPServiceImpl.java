package com.university.project.legendsofswordandwand.service.pvp.impl;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.PvPInvitation;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.model.enums.BattleStatus;
import com.university.project.legendsofswordandwand.model.enums.InvitationStatus;
import com.university.project.legendsofswordandwand.repository.PvPInvitationRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.pvp.IPvPService;
import java.time.LocalDateTime;
import com.university.project.legendsofswordandwand.service.hero.IHeroService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link IPvPService}, handling PvP invitation creation and acceptance.
 */
@Service
@RequiredArgsConstructor
class PvPServiceImpl implements IPvPService {

  private final UserRepository userRepository;
  private final PvPInvitationRepository pvpInvitationRepository;
  private final IHeroService heroService;

  /**
   * Creates a PvP battle invitation from the sender to the receiver.
   *
   * <p>Both players must have at least one saved party. A new {@link PvPInvitation} with status
   * {@link InvitationStatus#PENDING} is created and persisted.
   *
   * @param senderUsername the username of the player sending the invitation
   * @param receiverUsername the username of the player receiving the invitation
   * @throws RuntimeException if either user is not found, or if either player has no saved party
   */
  @Override
  public void createInvitation(String senderUsername, String receiverUsername) {

    if (senderUsername.equals(receiverUsername))
      throw new RuntimeException("You cannot challenge yourself.");

    User sender =
        userRepository
            .findByUsername(senderUsername)
            .orElseThrow(() -> new RuntimeException("Sender not found"));
    User receiver =
        userRepository
            .findByUsername(receiverUsername)
            .orElseThrow(() -> new RuntimeException("Receiver not found"));

    if (sender.getParties().stream().noneMatch(Party::isSaved))
      throw new RuntimeException("You must have at least one saved party to challenge");
    if (receiver.getParties().stream().noneMatch(Party::isSaved))
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

  /**
   * Accepts a pending PvP invitation by updating its status to {@link InvitationStatus#ACCEPTED}.
   *
   * @param inviteId the ID of the invitation to accept
   * @throws RuntimeException if the invitation is not found
   */
  @Override
  public void acceptInvitation(Long inviteId) {
    PvPInvitation invite =
        pvpInvitationRepository
            .findById(inviteId)
            .orElseThrow(() -> new RuntimeException("Invitation not found"));
    invite.setStatus(InvitationStatus.ACCEPTED);
    pvpInvitationRepository.save(invite);
  }

    @Override
    public void updatePvPResult(BattleState state) {
        if (!state.isPvp() || !state.isOver()) return;

        boolean senderWon = state.getStatus() == BattleStatus.PLAYER_WIN;

        String winnerUsername =
                senderWon ? state.getPvpSenderUsername() : state.getPvpReceiverUsername();
        String loserUsername =
                senderWon ? state.getPvpReceiverUsername() : state.getPvpSenderUsername();

        userRepository
                .findByUsername(winnerUsername)
                .ifPresent(
                        u -> {
                            u.setPvpWins(u.getPvpWins() + 1);
                            userRepository.save(u);
                        });
        userRepository
                .findByUsername(loserUsername)
                .ifPresent(
                        u -> {
                            u.setPvpLosses(u.getPvpLosses() + 1);
                            userRepository.save(u);
                        });

        // Restore all heroes in both parties to full HP/mana after PvP
        restorePartyHeroes(state.getPlayerUnits());
        restorePartyHeroes(state.getEnemyUnits());
    }

    private void restorePartyHeroes(List<BattleUnit> units) {
        units.forEach(
                u -> {
                    if (u.getHero().getId() == null) return; // skip enemy-generated units with no DB id
                    heroService
                            .findById(u.getHero().getId())
                            .ifPresent(
                                    hero -> {
                                        hero.setHealth(hero.getMaxHealth());
                                        hero.setMana(hero.getMaxMana());
                                        heroService.save(hero);
                                    });
                });
    }
}
