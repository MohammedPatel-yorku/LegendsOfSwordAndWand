package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.model.enums.RoomType;
import com.university.project.legendsofswordandwand.service.battle.IInnService;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignProgressService;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignService;
import com.university.project.legendsofswordandwand.service.hero.IHeroService;
import com.university.project.legendsofswordandwand.service.inventory.IInventoryService;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC controller handling inn-related requests, including healing, shopping, hero recruitment, and
 * continuing the campaign after an inn visit.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/inn")
public class InnController {

  private static final String RECRUITS_KEY = "innRecruits";
  private static final String LAST_RESULT_KEY = "lastBattleResult";

  private final IInnService innService;
  private final IHeroService heroService;
  private final ICampaignService campaignService;
  private final ICampaignProgressService campaignProgressService;
  private final IInventoryService inventoryService;

  /**
   * Serves the inn page, initialising healing, shop items, available recruits, and the party's
   * current inventory.
   *
   * <p>Access is permitted if the player is retreating after a loss, if an inn room is pending, or
   * if recruit data is already stored in the session. On the first arrival, the party is healed and
   * recruits are generated and cached in the session by ID to avoid stale entity references on
   * subsequent redirects. Redirects to {@code /campaign} if access conditions are not met or if an
   * error occurs.
   *
   * @param authentication the current user's authentication
   * @param model          the Spring MVC model
   * @param session        the current {@link HttpSession}
   * @return the logical view name for the inn page, or a redirect
   */
  @GetMapping
  public String innPage(Authentication authentication, Model model, HttpSession session) {
    if (authentication == null) return "redirect:/login";
    try {
      Campaign campaign = campaignService.getActiveCampaign(authentication.getName());
      if (!isInnAccessPermitted(campaign, session)) {
        return "redirect:/campaign";
      }

      if (session.getAttribute(RECRUITS_KEY) == null) {
        session.setAttribute("healSummary", innService.loadInnView(campaign.getId()));
      }

      List<Hero> recruits = loadOrRefreshRecruits(campaign.getId(), session);
      campaign = campaignService.getActiveCampaign(authentication.getName());
      populateInnModel(model, campaign, recruits, session);
    } catch (Exception e) {
      log.error("Unable to render inn page", e);
      return "redirect:/campaign";
    }
    return "campaign/inn";
  }

  private boolean isInnAccessPermitted(Campaign campaign, HttpSession session) {
    String lastResult = (String) session.getAttribute(LAST_RESULT_KEY);
    boolean retreatingAfterLoss = "PLAYER_LOSE".equals(lastResult);
    boolean innRoomPending = campaign.isRoomPending() && campaign.getLastRoomType() == RoomType.INN;
    return retreatingAfterLoss || innRoomPending || session.getAttribute(RECRUITS_KEY) != null;
  }

  private List<Hero> loadOrRefreshRecruits(Long campaignId, HttpSession session) {
    @SuppressWarnings("unchecked")
    List<Long> recruitIds = (List<Long>) session.getAttribute(RECRUITS_KEY);
    if (recruitIds == null) {
      List<Hero> freshRecruits = innService.getAvailableRecruits(campaignId);
      recruitIds = freshRecruits.stream().map(Hero::getId).toList();
      session.setAttribute(RECRUITS_KEY, recruitIds);
      return freshRecruits;
    }
    return recruitIds.stream()
        .map(id -> heroService.findById(id).orElse(null))
        .filter(h -> h != null && h.isTemporary())
        .toList();
  }

  private void populateInnModel(Model model, Campaign campaign, List<Hero> recruits, HttpSession session) {
    long permanentHeroCount =
        campaign.getParty().getHeroes().stream().filter(h -> !h.isTemporary()).count();

    model.addAttribute("healSummary", session.getAttribute("healSummary"));
    model.addAttribute(
        "heroes",
        campaign.getParty().getHeroes().stream().filter(h -> !h.isTemporary()).toList());
    model.addAttribute("gold", campaign.getParty().getGold());
    model.addAttribute("currentRoom", campaign.getCurrentRoom());
    model.addAttribute("shopItems", innService.getShopItems());
    model.addAttribute("availableRecruits", recruits);
    model.addAttribute("permanentHeroCount", permanentHeroCount);
    model.addAttribute("inventoryItems", inventoryService.getPartyInventoryItems(campaign.getId()));
    List<Hero> levelUpHeroes =
        campaign.getParty().getHeroes().stream()
            .filter(h -> !h.isTemporary())
            .filter(h -> heroService.isLevelUpPending(h.getId()))
            .toList();
    model.addAttribute("levelUpHeroes", levelUpHeroes);
    model.addAttribute("allHeroClasses", HeroClass.values());
  }

  /**
   * Handles a shop item purchase at the inn.
   *
   * <p>Adds a success or error flash attribute depending on whether the party had sufficient gold,
   * then redirects back to the inn.
   *
   * @param authentication     the current user's authentication
   * @param itemId             the ID of the shop item to purchase
   * @param redirectAttributes used to pass flash attributes across the redirect
   * @return a redirect to {@code /inn}
   */
  @PostMapping("/buy")
  public String buyItem(
          Authentication authentication,
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

  /**
   * Recruits a hero from the inn's available recruits and removes them from the session list.
   *
   * @param authentication     the current user's authentication
   * @param heroId             the ID of the hero to recruit
   * @param session            the current {@link HttpSession}
   * @param redirectAttributes used to pass flash attributes across the redirect
   * @return a redirect to {@code /inn}
   */
  @PostMapping("/recruit")
  public String recruitHero(
          Authentication authentication,
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

  /**
   * Concludes the inn visit, cleans up temporary recruits, and returns to the campaign.
   *
   * <p>Temporary recruits are removed from the party. Session attributes for recruits, heal summary,
   * and last battle result are cleared. If the player is continuing after a loss, the pending room
   * is intentionally kept so they retry the same battle room. Otherwise, the pending room is
   * cleared normally.
   *
   * <p>Exceptions are silently ignored to ensure the redirect always occurs.
   *
   * @param authentication the current user's authentication
   * @param session        the current {@link HttpSession}
   * @return a redirect to {@code /campaign}
   */
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
    } catch (Exception e) {
      log.error("Error cleaning up inn visit", e);
    }
    return "redirect:/campaign";
  }
}