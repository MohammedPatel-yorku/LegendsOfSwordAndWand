package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.service.ICampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** Rest Controller class mapped with '/campaign' to handle campaign requests. */
@Controller
@RequestMapping("/campaign")
@RequiredArgsConstructor
public class CampaignController {

  private final ICampaignService campaignService;

  @GetMapping("/new")
  public String newCampaignPage(Authentication authentication) {

    if (authentication == null) return "redirect:/login";
    return "new-campaign";
  }

  /**
   * Starts new campaign, mapped to POST '/start'.
   *
   * @param heroName Name to give to starting Hero
   * @param heroClass Hero Class to assign to starting Hero
   * @return New Campaign Object
   */
  @PostMapping("/new")
  public String startCampaign(
      Authentication authentication,
      @RequestParam("heroName") String heroName,
      @RequestParam("heroClass") HeroClass heroClass,
      Model model) {

    if (authentication == null) return "redirect:/login";

    if (heroName == null || heroName.isBlank()) {
      model.addAttribute("error", "Your hero needs a name, adventurer.");
      return "new-campaign";
    }

    try {
      campaignService.startNewCampaign(authentication.getName(), heroName.trim(), heroClass);
    } catch (Exception e) {
      model.addAttribute("error", "The campaign could not begin: " + e.getMessage());
      return "new-campaign";
    }

    return "redirect:/dashboard";
  }

  @GetMapping("/continue")
  public String continueCampaignPage(Authentication authentication) {
    if (authentication == null) return "redirect:/login";
    return "redirect:/dashboard";
  }
}
