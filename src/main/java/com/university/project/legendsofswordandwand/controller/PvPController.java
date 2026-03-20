package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.service.battle.IBattleService;
import com.university.project.legendsofswordandwand.service.pvp.IPvPService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller handling player-versus-player (PvP) battle requests,
 * including invitations and battle initiation.
 */
@RestController
@RequestMapping("/pvp")
@RequiredArgsConstructor
public class PvPController {

    private final IBattleService battleService;
    private final IPvPService pvpService;

    /**
     * Sends a PvP battle invitation from the authenticated user to the specified enemy.
     *
     * @param authentication the current user's authentication
     * @param enemyUsername  the username of the player to invite
     * @return {@code 200 OK} with a confirmation message
     */
    @PostMapping("/invite")
    public ResponseEntity<?> sendInvite(
            Authentication authentication, @RequestParam String enemyUsername) {

        pvpService.createInvitation(authentication.getName(), enemyUsername);
        return ResponseEntity.ok("Invitation Sent");
    }

    /**
     * Accepts a pending PvP battle invitation by its ID.
     *
     * @param inviteId the ID of the invitation to accept
     * @return {@code 200 OK} with a confirmation message
     */
    @PostMapping("/accept/{inviteId}")
    public ResponseEntity<?> acceptInvite(@PathVariable Long inviteId) {

        pvpService.acceptInvitation(inviteId);
        return ResponseEntity.ok("Invitation Accepted");
    }

    /**
     * Starts a PvP battle between the two parties associated with the given invitation.
     *
     * @param inviteId        the ID of the accepted invitation
     * @param senderPartyId   the ID of the inviting player's party
     * @param receiverPartyId the ID of the invited player's party
     */
    @PostMapping("/start")
    public void startBattle(Long inviteId, Long senderPartyId, Long receiverPartyId) {}
}