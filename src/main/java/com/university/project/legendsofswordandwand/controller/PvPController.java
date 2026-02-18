package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import com.university.project.legendsofswordandwand.service.BattleService;
import com.university.project.legendsofswordandwand.service.PvPService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pvp")
@RequiredArgsConstructor
public class PvPController {

  private final BattleService battleService;
  private final PartyRepository partyRepository;
  PvPService pvpService;

  @PostMapping("/invite")
  public ResponseEntity<?> sendInvite(@RequestParam String enemyUsername) {

    pvpService.createInvitation(1L, enemyUsername);
    return ResponseEntity.ok("Invitation Sent");
  }

  @PostMapping("/accept/{inviteId}")
  public ResponseEntity<?> acceptInvite(@PathVariable Long inviteId) {

    pvpService.acceptInvitation(inviteId);
    return ResponseEntity.ok("Invitation Accepted");
  }

  @PostMapping("/start")
  public void startBattle(Long inviteId, Long senderPartyId, Long receiverPartyId) {

    Party senderParty =
        partyRepository
            .findById(senderPartyId)
            .orElseThrow(() -> new RuntimeException("Party not found"));
    Party receiverParty =
        partyRepository
            .findById(receiverPartyId)
            .orElseThrow(() -> new RuntimeException("Party not found"));
  }
}
