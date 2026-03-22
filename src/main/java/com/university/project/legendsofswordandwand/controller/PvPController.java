package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.PvPInvitationRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.battle.IBattleService;
import com.university.project.legendsofswordandwand.service.pvp.IPvPService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pvp")
@RequiredArgsConstructor
public class PvPController {

  private static final String SESSION_KEY = "battleState";

  private final IPvPService pvpService;
  private final IBattleService battleService;
  private final UserRepository userRepository;
  private final PvPInvitationRepository pvpInvitationRepository;

  @GetMapping
  public String pvpPage(Authentication authentication, Model model) {
    if (authentication == null) return "redirect:/login";
    User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
    model.addAttribute("savedParties", user.getParties().stream().filter(Party::isSaved).toList());
    model.addAttribute(
        "pendingReceived",
        pvpInvitationRepository.findPendingByReceiverUsername(authentication.getName()));
    model.addAttribute(
        "pendingSent",
        pvpInvitationRepository.findPendingBySenderUsername(authentication.getName()));
    return "pvp/lobby";
  }

  @PostMapping("/invite")
  public String sendInvite(
      Authentication authentication,
      @RequestParam String enemyUsername,
      RedirectAttributes redirectAttributes) {
    if (authentication == null) return "redirect:/login";
    try {
      pvpService.createInvitation(authentication.getName(), enemyUsername);
      redirectAttributes.addFlashAttribute("message", "Challenge sent to " + enemyUsername + "!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/pvp";
  }

  @PostMapping("/accept/{inviteId}")
  public String acceptInvite(
      @PathVariable Long inviteId, Authentication authentication, Model model) {
    if (authentication == null) return "redirect:/login";
    pvpService.acceptInvitation(inviteId);
    User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
    model.addAttribute("inviteId", inviteId);
    model.addAttribute("step", 1); // receiver picks first
    model.addAttribute("savedParties", user.getParties().stream().filter(Party::isSaved).toList());
    return "pvp/select-party";
  }

  @PostMapping("/start")
  public String startBattle(
      @RequestParam Long inviteId,
      @RequestParam(required = false) Long senderPartyId,
      @RequestParam(required = false) Long receiverPartyId,
      @RequestParam int step,
      Authentication authentication,
      HttpSession session,
      Model model,
      RedirectAttributes redirectAttributes) {
    if (authentication == null) return "redirect:/login";

    if (step == 2) {
      // Receiver picked — now show party select for sender
      // Find the sender from the invitation
      var invite = pvpInvitationRepository.findById(inviteId).orElseThrow();
      User sender = invite.getSender();
      model.addAttribute("inviteId", inviteId);
      model.addAttribute("step", 2);
      model.addAttribute("receiverPartyId", receiverPartyId);
      model.addAttribute(
          "savedParties", sender.getParties().stream().filter(Party::isSaved).toList());
      return "pvp/select-party";
    }

    // step == 3: both parties chosen, start the battle
    try {
      BattleState state =
          battleService.initializePvPBattle(senderPartyId, receiverPartyId, inviteId);
      session.setAttribute("battleState", state);
      return "redirect:/battle";
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/pvp";
    }
  }
}
