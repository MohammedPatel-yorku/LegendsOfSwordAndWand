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

/**
 * MVC controller handling all PvP-related HTTP requests, including the lobby view, invitation
 * sending, invitation acceptance, and battle initialisation.
 */
@Controller
@RequestMapping("/pvp")
@RequiredArgsConstructor
public class PvPController {

  private static final String SESSION_KEY = "battleState";

  private final IPvPService pvpService;
  private final IBattleService battleService;
  private final UserRepository userRepository;
  private final PvPInvitationRepository pvpInvitationRepository;

  /**
   * Serves the PvP lobby page, showing the player's saved parties and pending invitations.
   *
   * @param authentication the current user's authentication
   * @param model the Spring MVC model
   * @return the logical view name for the PvP lobby page, or a redirect to login
   */
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

  /**
   * Sends a PvP challenge invitation to another player.
   *
   * @param authentication the current user's authentication
   * @param enemyUsername the username of the player to challenge
   * @param redirectAttributes used to pass success or error flash messages across the redirect
   * @return a redirect to the PvP lobby
   */
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

  /**
   * Accepts a pending PvP invitation and advances to party selection for the receiver.
   *
   * @param inviteId the ID of the invitation to accept
   * @param authentication the current user's authentication
   * @param model the Spring MVC model
   * @return the party selection view
   */
  @PostMapping("/accept/{inviteId}")
  public String acceptInvite(
          @PathVariable Long inviteId, Authentication authentication, Model model) {
    if (authentication == null) return "redirect:/login";
    pvpService.acceptInvitation(inviteId);
    User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
    model.addAttribute("inviteId", inviteId);
    model.addAttribute("step", 1);
    model.addAttribute("savedParties", user.getParties().stream().filter(Party::isSaved).toList());
    return "pvp/select-party";
  }

  /**
   * Handles each step of the two-player party selection flow and, on the final step, initialises
   * the PvP battle and stores it in the session.
   *
   * <p>Step 1: receiver selects their party. Step 2: sender selects their party. Step 3: both
   * parties confirmed — battle begins.
   *
   * @param inviteId the ID of the PvP invitation
   * @param senderPartyId the ID of the sender's chosen party (set at step 3)
   * @param receiverPartyId the ID of the receiver's chosen party (set at step 2)
   * @param step the current step in the party selection flow (1, 2, or 3)
   * @param authentication the current user's authentication
   * @param session the current {@link HttpSession}
   * @param model the Spring MVC model
   * @param redirectAttributes used to pass error flash messages across the redirect
   * @return a redirect to {@code /battle} on success, or the select-party view for intermediate
   *     steps
   */
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
      session.setAttribute(SESSION_KEY, state);
      return "redirect:/battle";
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/pvp";
    }
  }
}