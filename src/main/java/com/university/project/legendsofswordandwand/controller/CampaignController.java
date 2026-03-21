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

  /**
   * Serves the new campaign creation page.
   *
   * @param authentication the current user's authentication
   * @return the logical view name for the new campaign page, or a redirect to login
   */
  @GetMapping("/new")
  public String newCampaignPage(Authentication authentication) {
    if (authentication == null) return "redirect:/login";
    return "campaign/new-campaign";
  }

  /**
   * Starts new campaign, mapped to POST '/start'.
   *
   * @param authentication the current user's authentication
   * @param heroName name to give to the starting hero
   * @param heroClass hero class to assign to the starting hero
   * @param model the Spring MVC model
   * @return a redirect to {@code /campaign} on success, or the new campaign view on failure
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

  /**
   * Serves the main campaign page, populating the model with the current room, heroes, gold,
   * inventory, and any heroes with a pending level-up.
   *
   * <p>Redirects to {@code /dashboard} if no active campaign is found.
   *
   * @param authentication the current user's authentication
   * @param model the Spring MVC model
   * @return the logical view name for the campaign page, or a redirect on failure
   */
  @GetMapping
  public String campaignPage(Authentication authentication, Model model) {
    if (authentication == null) return "redirect:/login";
    try {
      Campaign campaign = campaignService.getActiveCampaign(authentication.getName());
      CampaignViewInfo data = campaignProgressService.getCampaignViewData(authentication.getName());
      model.addAttribute(
          "inventoryItems", inventoryService.getPartyInventoryItems(campaign.getId()));
      model.addAttribute("heroes", data.heroes());
      model.addAttribute("currentRoom", data.currentRoom());
      model.addAttribute("gold", data.gold());

      // Level up panel
      model.addAttribute(
          "levelUpHeroes",
          data.heroes().stream().filter(h -> heroService.isLevelUpPending(h.getId())).toList());
      model.addAttribute("allHeroClasses", HeroClass.values());
    } catch (Exception e) {
      return "redirect:/dashboard";
    }
    return "campaign/campaign";
  }

  /**
   * Advances the campaign to the next room and redirects accordingly.
   *
   * <p>Redirects to {@code /battle} if the next room is a battle, or {@code /inn} otherwise.
   * Redirects to {@code /campaign} if progression fails.
   *
   * @param authentication the current user's authentication
   * @return a redirect to the appropriate room destination
   */
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

  /**
   * Exits the active campaign and returns to the dashboard.
   *
   * <p>Prevents exit if a battle room is currently pending, adding an error flash attribute and
   * redirecting back to the campaign page in that case.
   *
   * @param authentication the current user's authentication
   * @param redirectAttributes used to pass flash attributes across the redirect
   * @return a redirect to {@code /dashboard} on success, or {@code /campaign} on failure
   */
  @PostMapping("/exit")
  public String exitCampaign(Authentication authentication, RedirectAttributes redirectAttributes) {
    if (authentication == null) return "redirect:/login";
    try {
      Campaign campaign = campaignService.getActiveCampaign(authentication.getName());

      if (campaign.isRoomPending() && campaign.getLastRoomType() == RoomType.BATTLE) {
        redirectAttributes.addFlashAttribute("error", "You cannot exit during a battle.");
        return "redirect:/campaign";
      }

      campaignService.exitCampaign(authentication.getName());
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/campaign";
    }

    return "redirect:/dashboard";
  }

  /**
   * Abandons the active campaign and returns to the dashboard.
   *
   * <p>Exceptions are silently ignored to ensure the redirect always occurs.
   *
   * @param authentication the current user's authentication
   * @return a redirect to {@code /dashboard}
   */
  @PostMapping("/abandon")
  public String abandonCampaign(Authentication authentication) {
    if (authentication == null) return "redirect:/login";
    try {
      campaignService.abandonCampaign(authentication.getName());
    } catch (Exception ignored) {
    }
    return "redirect:/dashboard";
  }

  /**
   * Uses a consumable item from the party inventory on the specified hero.
   *
   * @param authentication the current user's authentication
   * @param heroId the ID of the hero to use the item on
   * @param itemId the ID of the inventory item to use
   * @param redirectAttributes used to pass flash attributes across the redirect
   * @return a redirect to {@code /campaign}
   */
  @PostMapping("/use-item")
  public String useItem(
      Authentication authentication,
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

  /**
   * Applies a level-up to the specified hero using the given {@link HeroClass}.
   *
   * <p>Redirects to the page specified by {@code returnTo} after the level-up, defaulting to {@code
   * /campaign}.
   *
   * @param authentication the current user's authentication
   * @param heroId the ID of the hero to level up
   * @param heroClass the {@link HeroClass} to apply for the level-up bonus
   * @param returnTo the path to redirect to after the level-up (default: {@code campaign})
   * @param redirectAttributes used to pass flash attributes across the redirect
   * @return a redirect to the specified return destination
   */
  @PostMapping("/level-up")
  public String levelUp(
      Authentication authentication,
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

  /**
   * Serves the campaign completion page, displaying score, gold, heroes, and save options.
   *
   * <p>Redirects to {@code /dashboard} if completion data cannot be retrieved.
   *
   * @param authentication the current user's authentication
   * @param model the Spring MVC model
   * @return the logical view name for the completion page, or a redirect on failure
   */
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

  /**
   * Saves the party from the completed campaign as a new saved party.
   *
   * @param authentication the current user's authentication
   * @param campaignId the ID of the completed campaign
   * @param model the Spring MVC model
   * @return a redirect to {@code /dashboard} on success, or {@code /campaign/complete} on failure
   */
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

  /**
   * Replaces an existing saved party with the party from the completed campaign.
   *
   * <p>Exceptions are silently ignored to ensure the redirect always occurs.
   *
   * @param authentication the current user's authentication
   * @param campaignId the ID of the completed campaign
   * @param replacePartyId the ID of the saved party to replace
   * @return a redirect to {@code /dashboard}
   */
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

  /**
   * Discards the party from the completed campaign without saving and returns to the dashboard.
   *
   * @return a redirect to {@code /dashboard}
   */
  @PostMapping("/complete/discard")
  public String discardParty() {
    return "redirect:/dashboard";
  }
}
