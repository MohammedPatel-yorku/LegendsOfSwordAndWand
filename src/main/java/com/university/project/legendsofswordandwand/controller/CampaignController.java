package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.dto.response.CampaignViewInfo;
import com.university.project.legendsofswordandwand.dto.response.CompleteCampaignInfo;
import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.model.enums.RoomType;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignProgressService;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignService;
import com.university.project.legendsofswordandwand.service.hero.IHeroService;
import com.university.project.legendsofswordandwand.service.inventory.IInventoryService;
import com.university.project.legendsofswordandwand.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Rest Controller class mapped with '/campaign' to handle campaign requests. */
@Controller
@RequestMapping("/campaign")
@RequiredArgsConstructor
public class CampaignController {

  private final ICampaignService campaignService;
  private final ICampaignProgressService campaignProgressService;
  private final IUserService userService;
  private final IInventoryService inventoryService;
  private final IHeroService heroService;

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
      Campaign campaign = campaignService.getActiveCampaign(authentication.getName());
      CampaignViewInfo data = campaignProgressService.getCampaignViewData(authentication.getName());
      model.addAttribute("inventoryItems", inventoryService.getPartyInventoryItems(campaign.getId()));
      model.addAttribute("heroes", data.heroes());
      model.addAttribute("currentRoom", data.currentRoom());
      model.addAttribute("gold", data.gold());

      // Level up panel
      model.addAttribute("levelUpHeroes", data.heroes().stream()
              .filter(h -> heroService.isLevelUpPending(h.getId()))
              .toList());
      model.addAttribute("allHeroClasses", HeroClass.values());
    } catch (Exception e) {
      return "redirect:/dashboard";
    }
    return "campaign/campaign";
  }

  @PostMapping("/next-room")
  public String nextRoom(Authentication authentication) {

    if (authentication == null) return "redirect:/login";

    try {

      RoomType room = campaignProgressService.enterNextRoom(authentication.getName());
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

  @PostMapping("/abandon")
  public String abandonCampaign(Authentication authentication) {
    if (authentication == null) return "redirect:/login";
    try {
      campaignService.abandonCampaign(authentication.getName());
    } catch (Exception ignored) {}
    return "redirect:/dashboard";
  }

  @PostMapping("/use-item")
  public String useItem(Authentication authentication,
                        @RequestParam Long heroId,
                        @RequestParam Long itemId,
                        RedirectAttributes redirectAttributes) {
    if (authentication == null) return "redirect:/login";
    try {
      Campaign campaign = campaignService.getActiveCampaign(authentication.getName());
      inventoryService.useItem(campaign.getId(), heroId, itemId);
      redirectAttributes.addFlashAttribute("message", "Item used.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/campaign";
  }

  @PostMapping("/level-up")
  public String levelUp(Authentication authentication,
                        @RequestParam Long heroId,
                        @RequestParam HeroClass heroClass,
                        @RequestParam(defaultValue = "campaign") String returnTo,
                        RedirectAttributes redirectAttributes) {
    if (authentication == null) return "redirect:/login";
    try {
      heroService.levelUp(heroId, heroClass);
      redirectAttributes.addFlashAttribute("message", "Hero levelled up!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/" + returnTo;
  }

  @GetMapping("/complete")
  public String completePage(Authentication authentication, Model model) {

    if (authentication == null) return "redirect:/login";

    try {

      CompleteCampaignInfo data =
          campaignProgressService.getCompletionData(authentication.getName());
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
