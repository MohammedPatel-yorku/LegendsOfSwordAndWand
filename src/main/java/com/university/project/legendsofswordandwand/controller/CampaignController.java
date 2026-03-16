package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.dto.response.CampaignViewInfo;
import com.university.project.legendsofswordandwand.dto.response.CompleteCampaignInfo;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.model.enums.RoomType;
import com.university.project.legendsofswordandwand.service.ICampaignService;
import com.university.project.legendsofswordandwand.service.IUserService;
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
  private final IUserService userService;

  @GetMapping("/new")
  public String newCampaignPage(Authentication authentication) {

    if (authentication == null) return "redirect:/login";
    return "campaign/new-campaign";
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
      return "campaign/new-campaign";
    }

    try {
      campaignService.startNewCampaign(authentication.getName(), heroName.trim(), heroClass);
    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
      return "campaign/new-campaign";
    }

    return "redirect:/campaign";
  }

  @GetMapping
  public String campaignPage(Authentication authentication, Model model) {
    if (authentication == null) return "redirect:/login";

    try {

      CampaignViewInfo data = campaignService.getCampaignViewData(authentication.getName());
      model.addAttribute("heroes", data.heroes());
      model.addAttribute("currentRoom", data.currentRoom());
      model.addAttribute("gold", data.gold());
    } catch (Exception e) {

      return "redirect:/dashboard";
    }

    return "campaign/campaign";
  }

  @PostMapping("/next-room")
  public String nextRoom(Authentication authentication) {

    if (authentication == null) return "redirect:/login";

    try {

      RoomType room = campaignService.enterNextRoom(authentication.getName());
      return room == RoomType.BATTLE ? "redirect:/battle" : "redirect:/inn";
    } catch (Exception e) {

      return "redirect:/campaign";
    }
  }

  @PostMapping("/exit")
  public String exitCampaign(Authentication authentication) {

    if (authentication == null) return "redirect:/login";

    try {

      campaignService.exitCampaign(authentication.getName());
    } catch (Exception ignored) {
    }

    return "redirect:/dashboard";
  }

  @GetMapping("/complete")
  public String completePage(Authentication authentication, Model model) {

    if (authentication == null) return "redirect:/login";

    try {

      CompleteCampaignInfo data = campaignService.getCompletionData(authentication.getName());
      model.addAttribute("campaignId", data.campaignId());
      model.addAttribute("score", data.score());
      model.addAttribute("gold", data.gold());
      model.addAttribute("heroes", data.heroes());
      model.addAttribute("partyFull", data.partyFull());
      model.addAttribute("savedParties", data.savedParties());
    } catch (Exception e) {
      return "redirect:/dashboard";
    }

    return "campaign/complete";
  }

  @PostMapping("/complete/save")
  public String saveParty(
      Authentication authentication, @RequestParam("campaignId") Long campaignId, Model model) {

    if (authentication == null) return "redirect:/login";

    try {

      Long userId = userService.getUserIdByUsername(authentication.getName());
      campaignService.savePartyFromCampaign(campaignId, userId);
    } catch (Exception e) {

      model.addAttribute("error", e.getMessage());
      return "redirect:/campaign/complete";
    }

    return "redirect:/dashboard";
  }

  @PostMapping("/complete/replace")
  public String replaceParty(
      Authentication authentication,
      @RequestParam("campaignId") Long campaignId,
      @RequestParam("replacePartyId") Long replacePartyId) {

    if (authentication == null) return "redirect:/login";

    try {

      Long userId = userService.getUserIdByUsername(authentication.getName());
      campaignService.replacePartyFromCampaign(campaignId, userId, replacePartyId);
    } catch (Exception e) {
    }

    return "redirect:/dashboard";
  }

  @PostMapping("/complete/discard")
  public String discardParty() {
    return "redirect:/dashboard";
  }
}
