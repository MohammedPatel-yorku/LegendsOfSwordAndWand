package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.service.battle.IInnService;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignProgressService;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignService;
import jakarta.servlet.http.HttpSession;
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

  private final IInnService innService;
  private final ICampaignService campaignService;
  private final ICampaignProgressService campaignProgressService;

  @GetMapping
  public String innPage(Authentication authentication, Model model, HttpSession session) {
    if (authentication == null) return "redirect:/login";
    try {
      Campaign campaign = campaignService.getActiveCampaign(authentication.getName());

      // Only heal on first visit, not on every buy/recruit redirect
      if (session.getAttribute(RECRUITS_KEY) == null) {
        innService.loadInnView(campaign.getId());
      }

      // Generate recruits once per inn visit
      @SuppressWarnings("unchecked")
      List<Hero> recruits = (List<Hero>) session.getAttribute(RECRUITS_KEY);
      if (recruits == null) {
        recruits = innService.getAvailableRecruits(campaign.getId());
        session.setAttribute(RECRUITS_KEY, recruits);
      }

      // Refresh campaign so hero HP/mana reflects the heal
      campaign = campaignService.getActiveCampaign(authentication.getName());

      long permanentHeroCount = campaign.getParty().getHeroes().stream()
              .filter(h -> !h.isTemporary()).count();

      if (session.getAttribute(RECRUITS_KEY) == null) {
        List<String> healSummary = innService.loadInnView(campaign.getId());
        session.setAttribute("healSummary", healSummary);
      }

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

      // Remove from session list so they don't show as available anymore
      @SuppressWarnings("unchecked")
      List<Hero> recruits = (List<Hero>) session.getAttribute(RECRUITS_KEY);
      if (recruits != null) {
        recruits.removeIf(h -> h.getId().equals(heroId));
        session.setAttribute(RECRUITS_KEY, recruits);
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
      campaignProgressService.clearRoomPending(authentication.getName());
      session.removeAttribute(RECRUITS_KEY);
    } catch (Exception ignored) {}
    return "redirect:/campaign";
  }
}