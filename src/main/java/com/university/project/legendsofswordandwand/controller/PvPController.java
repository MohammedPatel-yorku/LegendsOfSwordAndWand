package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.service.IBattleService;
import com.university.project.legendsofswordandwand.service.IPvPService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pvp")
@RequiredArgsConstructor
public class PvPController {

  private final IBattleService battleService;
  private final IPvPService pvpService;

  @PostMapping("/invite")
  public ResponseEntity<?> sendInvite(
      Authentication authentication, @RequestParam String enemyUsername) {

    pvpService.createInvitation(authentication.getName(), enemyUsername);
    return ResponseEntity.ok("Invitation Sent");
  }

  @PostMapping("/accept/{inviteId}")
  public ResponseEntity<?> acceptInvite(@PathVariable Long inviteId) {

    pvpService.acceptInvitation(inviteId);
    return ResponseEntity.ok("Invitation Accepted");
  }

  @PostMapping("/start")
  public void startBattle(Long inviteId, Long senderPartyId, Long receiverPartyId) {}
}
