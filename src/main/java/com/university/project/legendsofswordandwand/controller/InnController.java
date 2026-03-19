package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.service.battle.IInnService;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignProgressService;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignService;
import com.university.project.legendsofswordandwand.service.hero.IHeroService;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/inn")
public class InnController {

  private static final String RECRUITS_KEY = "innRecruits";
  private static final String LAST_RESULT_KEY = "lastBattleResult";

  private final IInnService innService;
  private final IHeroService heroService;
  private final ICampaignService campaignService;
  private final ICampaignProgressService campaignProgressService;

  @GetMapping
  public String innPage(Authentication authentication, Model model, HttpSession session) {
    if (authentication == null) return "redirect:/login";
    try {
      Campaign campaign = campaignService.getActiveCampaign(authentication.getName());

      // Heal only on first arrival, not on every buy/recruit redirect
      if (session.getAttribute(RECRUITS_KEY) == null) {
        List<String> healSummary = innService.loadInnView(campaign.getId());
        session.setAttribute("healSummary", healSummary);
      }

      // Generate recruits once per inn visit, store IDs to avoid stale entities
      @SuppressWarnings("unchecked")
      List<Long> recruitIds = (List<Long>) session.getAttribute(RECRUITS_KEY);
      List<Hero> recruits;
      if (recruitIds == null) {
        List<Hero> freshRecruits = innService.getAvailableRecruits(campaign.getId());
        recruitIds = freshRecruits.stream().map(Hero::getId).toList();
        session.setAttribute(RECRUITS_KEY, recruitIds);
        recruits = freshRecruits;
      } else {
        // Reload from DB — filter out any already recruited
        recruits = recruitIds.stream()
                .map(id -> heroService.findById(id).orElse(null))
                .filter(h -> h != null && h.isTemporary())
                .toList();
      }

      // Refresh campaign after potential heal
      campaign = campaignService.getActiveCampaign(authentication.getName());

      long permanentHeroCount = campaign.getParty().getHeroes().stream()
              .filter(h -> !h.isTemporary()).count();

      model.addAttribute("healSummary", session.getAttribute("healSummary"));
      model.addAttribute("heroes", campaign.getParty().getHeroes().stream()
              .filter(h -> !h.isTemporary()).toList());
      model.addAttribute("gold", campaign.getParty().getGold());
      model.addAttribute("currentRoom", campaign.getCurrentRoom());
      model.addAttribute("shopItems", innService.getShopItems());
      model.addAttribute("availableRecruits", recruits);
      model.addAttribute("permanentHeroCount", permanentHeroCount);
    } catch (Exception e) {
      return "redirect:/campaign";
    }
    return "campaign/inn";
  }

  @PostMapping("/buy")
  public String buyItem(Authentication authentication,
                        @RequestParam Long itemId,
                        RedirectAttributes redirectAttributes) {
    if (authentication == null) return "redirect:/login";
    try {
      Campaign campaign = campaignService.getActiveCampaign(authentication.getName());
      boolean ok = innService.purchaseItem(campaign.getId(), itemId);
      if (!ok) redirectAttributes.addFlashAttribute("error", "Not enough gold.");
      else redirectAttributes.addFlashAttribute("message", "Item purchased.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/inn";
  }

  @PostMapping("/recruit")
  public String recruitHero(Authentication authentication,
                            @RequestParam Long heroId,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
    if (authentication == null) return "redirect:/login";
    try {
      Campaign campaign = campaignService.getActiveCampaign(authentication.getName());
      innService.recruitHero(campaign.getId(), heroId);

      @SuppressWarnings("unchecked")
      List<Long> recruitIds = (List<Long>) session.getAttribute(RECRUITS_KEY);
      if (recruitIds != null) {
        recruitIds = new ArrayList<>(recruitIds);
        recruitIds.remove(heroId);
        session.setAttribute(RECRUITS_KEY, recruitIds);
      }

      redirectAttributes.addFlashAttribute("message", "Hero recruited!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/inn";
  }

  @PostMapping("/continue")
  public String continueFromInn(Authentication authentication, HttpSession session) {
    if (authentication == null) return "redirect:/login";
    try {
      Campaign campaign = campaignService.getActiveCampaign(authentication.getName());
      innService.cleanupTemporaryRecruits(campaign.getId());
      session.removeAttribute(RECRUITS_KEY);
      session.removeAttribute("healSummary");

      String lastResult = (String) session.getAttribute(LAST_RESULT_KEY);
      session.removeAttribute(LAST_RESULT_KEY);

      if ("PLAYER_LOSE".equals(lastResult)) {
        // Keep room pending so player retries the same battle room
        // Do NOT call clearRoomPending
      } else {
        campaignProgressService.clearRoomPending(authentication.getName());
      }
    } catch (Exception ignored) {}
    return "redirect:/campaign";
  }
}