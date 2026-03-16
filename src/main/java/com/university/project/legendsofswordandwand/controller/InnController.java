package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.service.battle.IInnService;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignProgressService;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/inn")
public class InnController {

  private final IInnService innService;
  private final ICampaignService campaignService;
  private final ICampaignProgressService campaignProgressService;

  @GetMapping
  public String innPage(Authentication authentication, Model model) {

    if (authentication == null) return "redirect:/login";

    try {

      Campaign campaign = campaignService.getActiveCampaign(authentication.getName());
      innService.loadInnView(campaign.getId());
      model.addAttribute("campaign", campaign);
      model.addAttribute("heroes", campaign.getParty().getHeroes());
      model.addAttribute("gold", campaign.getParty().getGold());
      model.addAttribute("currentRoom", campaign.getCurrentRoom());
      model.addAttribute("shopItems", innService.getShopItems());
      model.addAttribute("availableRecruits", innService.getAvailableRecruits(campaign.getId()));
    } catch (Exception e) {

      return "redirect:/campaign";
    }

    return "campaign/inn";
  }
}
