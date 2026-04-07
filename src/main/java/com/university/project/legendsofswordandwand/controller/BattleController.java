package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.SessionConstants;
import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.HeroSnapshot;
import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.ActionType;
import com.university.project.legendsofswordandwand.model.enums.BattleStatus;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.service.battle.IBattleRewardService;
import com.university.project.legendsofswordandwand.service.battle.IBattleService;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignProgressService;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignService;
import com.university.project.legendsofswordandwand.service.hero.IHeroService;
import com.university.project.legendsofswordandwand.service.pvp.IPvPService;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * MVC controller handling all battle-related HTTP requests, including battle initialisation, player
 * actions, result display, and campaign progression.
 */
@Controller
@RequestMapping("/battle")
@RequiredArgsConstructor
public class BattleController {

  private static final String SESSION_KEY = "battleState";

  private final IBattleService battleService;
  private final IPvPService pvPService;
  private final ICampaignService campaignService;
  private final ICampaignProgressService campaignProgressService;
  private final IHeroService heroService;
  private final IBattleRewardService battleRewardService;

  /**
   * Serves the battle page, initialising a new PvE battle if one is not already in progress.
   *
   * <p>If no active {@link BattleState} exists in the session, a new battle is created for the
   * player's active campaign. Enemy turns are automatically processed until it becomes the player's
   * turn. Redirects to {@code /campaign} if initialisation fails.
   *
   * @param authentication the current user's authentication
   * @param session the current {@link HttpSession}
   * @return the logical view name for the battle page, or a redirect on failure
   */
  @GetMapping
  public String battlePage(Authentication authentication, HttpSession session, Model model) {
    if (authentication == null) return "redirect:/login";
    try {
      BattleState state = (BattleState) session.getAttribute(SESSION_KEY);

      if (state != null && state.isPvp() && state.isOver()) {
        return "redirect:/battle/result";
      }

      if (state == null || state.isOver()) {
        var campaign = campaignService.getActiveCampaign(authentication.getName());
        int cumulativeLevel = campaignService.getPartyCumulativeLevel(authentication.getName());
        state = battleService.initializePvEBattle(campaign.getId(), cumulativeLevel);
        state = runEnemyTurns(state); // Smell 1 fix: extracted from duplicated block
        session.setAttribute(SESSION_KEY, state);
      }

      model.addAttribute("isPvp", state != null && state.isPvp());
    } catch (Exception e) {
      return "redirect:/campaign";
    }
    return "battle/battle";
  }

  /**
   * Returns the current {@link BattleState} as a JSON DTO.
   *
   * @param authentication the current user's authentication
   * @param session the current {@link HttpSession}
   * @return {@code 200 OK} with the state DTO, {@code 401} if unauthenticated, or {@code 404} if no
   *     battle is in session
   */
  @GetMapping("/state")
  @ResponseBody
  public ResponseEntity<?> getState(Authentication authentication, HttpSession session) {
    if (authentication == null) return ResponseEntity.status(401).build();
    BattleState state = (BattleState) session.getAttribute(SESSION_KEY);
    if (state == null) return ResponseEntity.status(404).build();
    return ResponseEntity.ok(toDto(state));
  }

  /**
   * Processes a player action and advances the battle until the next player turn.
   *
   * <p>Executes the given {@link ActionType} for the active player unit, then automatically
   * processes consecutive enemy turns until it is the player's turn again or the battle ends. The
   * updated state is always saved back to the session.
   *
   * @param authentication the current user's authentication
   * @param session the current {@link HttpSession}
   * @param action the {@link ActionType} the player wishes to perform
   * @param targetBattleId the battle ID of the target unit, if required by the action
   * @param abilityIndex the ability slot index, if the action is an ability cast
   * @return {@code 200 OK} with the updated state DTO, {@code 400} on invalid action, or {@code
   *     401} if unauthenticated
   */
  @PostMapping("/action")
  @ResponseBody
  public ResponseEntity<?> action(
      Authentication authentication,
      HttpSession session,
      @RequestParam ActionType action,
      @RequestParam(required = false) Long targetBattleId,
      @RequestParam(required = false) Integer abilityIndex) {

    if (authentication == null) return ResponseEntity.status(401).build();

    BattleState state = (BattleState) session.getAttribute(SESSION_KEY);
    if (state == null) return ResponseEntity.status(404).build();

    try {
      state = battleService.executePlayerAction(state, action, targetBattleId, abilityIndex);

      if (!state.isPvp()) {
        state = runEnemyTurns(state); // Smell 1 fix: extracted from duplicated block
      }
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } finally {
      session.setAttribute(SESSION_KEY, state);
    }

    return ResponseEntity.ok(toDto(state));
  }

  /**
   * Serves the battle result page, awarding rewards or applying losses as appropriate.
   *
   * <p>On a player win, gold and experience rewards are distributed once (guarded by a session
   * flag), and any heroes with a pending level-up are identified for the view. On a player loss,
   * loss penalties are applied once. Redirects to {@code /battle} if the battle is not yet over.
   *
   * @param authentication the current user's authentication
   * @param model the Spring MVC model
   * @param session the current {@link HttpSession}
   * @return the logical view name for the result page, or a redirect
   */
  @GetMapping("/result")
  public String result(Authentication authentication, Model model, HttpSession session) {
    if (authentication == null) return "redirect:/login";

    BattleState state = (BattleState) session.getAttribute(SESSION_KEY);
    if (state == null || !state.isOver()) return "redirect:/battle";

    try {

      boolean rewardsAlreadyGiven = Boolean.TRUE.equals(session.getAttribute("rewardsGiven"));
      if (!rewardsAlreadyGiven && state.getStatus() == BattleStatus.PLAYER_WIN) {
        Map<String, Object> rewards = battleRewardService.awardBattleRewards(state);
        model.addAttribute("rewardGold", rewards.get("gold"));
        model.addAttribute("rewardRecipients", rewards.get("recipients"));
        session.setAttribute("rewardsGiven", true);
      } else if (state.getStatus() == BattleStatus.PLAYER_WIN) {
        // Rewards already given, just show level up panel
        model.addAttribute("rewardGold", 0);
        model.addAttribute("rewardRecipients", List.of());
      }

      if (!rewardsAlreadyGiven && state.getStatus() == BattleStatus.PLAYER_LOSE) {
        battleRewardService.applyBattleLoss(state);
        session.setAttribute("rewardsGiven", true);
      }

      if (state.getStatus() == BattleStatus.PLAYER_WIN) {
        List<Hero> levelUpHeroes =
            state.getPlayerUnits().stream()
                .filter(u -> u.isAlive() && u.getHero().getId() != null)
                .filter(u -> heroService.isLevelUpPending(u.getHero().getId()))
                .map(u -> heroService.findById(u.getHero().getId()).orElse(null))
                .filter(Objects::nonNull)
                .toList();
        model.addAttribute("levelUpHeroes", levelUpHeroes);
        model.addAttribute("allHeroClasses", HeroClass.values());
      }

      model.addAttribute("status", state.getStatus());
      model.addAttribute("playerUnits", state.getPlayerUnits());
      model.addAttribute("enemyUnits", state.getEnemyUnits());
      boolean campaignDone =
          !state.isPvp() && campaignProgressService.isCampaignComplete(authentication.getName());
      model.addAttribute("campaignDone", campaignDone);
      model.addAttribute("isPvp", state.isPvp());
      if (state.isPvp()) {
        model.addAttribute("pvpSenderUsername", state.getPvpSenderUsername());
        model.addAttribute("pvpReceiverUsername", state.getPvpReceiverUsername());
      }
      if (state.isPvp() && !rewardsAlreadyGiven) {
        pvPService.updatePvPResult(state);
        session.setAttribute("rewardsGiven", true);
      }
      session.setAttribute(SessionConstants.LAST_RESULT, state.getStatus().name());
    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
    }

    return "battle/result";
  }

  /**
   * Handles post-battle campaign progression after the result page is acknowledged.
   *
   * <p>Clears the battle state and rewards flag from the session. If the campaign is complete,
   * finalises it and redirects to the completion page. If the player lost and has visited the inn,
   * redirects there for recovery. Otherwise, clears the pending room and redirects to the campaign
   * map.
   *
   * @param authentication the current user's authentication
   * @param session the current {@link HttpSession}
   * @return a redirect to the appropriate next destination
   */
  @PostMapping("/continue")
  public String continueCampaign(Authentication authentication, HttpSession session) {
    if (authentication == null) return "redirect:/login";

    BattleState state = (BattleState) session.getAttribute(SESSION_KEY);
    boolean wasPvp = state != null && state.isPvp();

    session.removeAttribute(SESSION_KEY);
    session.removeAttribute("rewardsGiven");

    if (wasPvp) {
      session.removeAttribute(SessionConstants.LAST_RESULT);
      return "redirect:/pvp";
    }

    try {
      String lastResult = (String) session.getAttribute(SessionConstants.LAST_RESULT);

      if (campaignProgressService.isCampaignComplete(authentication.getName())) {
        session.removeAttribute(SessionConstants.LAST_RESULT);
        campaignService.completeCampaign(authentication.getName());
        return "redirect:/campaign/complete";
      }

      if ("PLAYER_LOSE".equals(lastResult)) {
        Campaign campaign = campaignService.getActiveCampaign(authentication.getName());
        if (campaign.isHasVisitedInn()) {
          return "redirect:/inn";
        } else {
          // No inn visited yet — just clear the pending room and return to campaign
          session.removeAttribute(SessionConstants.LAST_RESULT);
          campaignService.abandonCampaign(authentication.getName());
          return "redirect:/dashboard";
        }
      }

      session.removeAttribute(SessionConstants.LAST_RESULT);
      campaignProgressService.clearRoomPending(authentication.getName());
    } catch (Exception ignored) {
    }
    return "redirect:/campaign";
  }

  /**
   * Handles a PvP forfeit request, forcing a loss for the forfeiting player.
   *
   * <p>Sets the battle status to {@link BattleStatus#PLAYER_LOSE} and immediately updates the PvP
   * win/loss records for both players. The battle state and rewards flag are then cleared from the
   * session. Non-PvP battles cannot be forfeited — requests without an active PvP battle in session
   * are redirected to the dashboard.
   *
   * @param authentication the current user's authentication
   * @param session the current {@link HttpSession}, used to retrieve and clear the active {@link
   *     BattleState}
   * @return a redirect to {@code /pvp} after the forfeit is processed, or {@code /dashboard} if no
   *     active PvP battle is found
   */
  @PostMapping("/forfeit")
  public String forfeit(Authentication authentication, HttpSession session) {
    if (authentication == null) return "redirect:/login";

    BattleState state = (BattleState) session.getAttribute(SESSION_KEY);
    if (state == null || !state.isPvp()) return "redirect:/dashboard";

    state.setStatus(BattleStatus.PLAYER_LOSE);
    session.setAttribute(SESSION_KEY, state);
    session.removeAttribute("rewardsGiven");
    return "redirect:/battle/result";
  }

  /**
   * Runs consecutive enemy turns until it is the player's turn or the battle ends.
   *
   * <p>A safety limit of 50 iterations prevents infinite loops in degenerate states. If the limit
   * is exhausted, and it is still not the player's turn, the battle status is forcibly rechecked.
   *
   * <p>Extracted from the duplicated blocks in {@link #battlePage} and {@link #action} to eliminate
   * code duplication (Smell 1 refactoring).
   *
   * @param state the current {@link BattleState}
   * @return the updated {@link BattleState} after all enemy turns have been processed
   */
  private BattleState runEnemyTurns(BattleState state) {
    int safetyLimit = 50;
    while (!state.isOver() && !state.isPlayerTurn() && safetyLimit-- > 0) {
      state = battleService.executeEnemyTurn(state);
    }
    if (!state.isOver() && !state.isPlayerTurn()) {
      state.setStatus(battleService.checkBattleStatus(state));
    }
    return state;
  }

  /**
   * Converts a {@link BattleState} into a plain map suitable for JSON serialisation.
   *
   * @param state the {@link BattleState} to convert
   * @return a map representation of the battle state
   */
  private Map<String, Object> toDto(BattleState state) {
    return Map.of(
        "over",
        state.isOver(),
        "status",
        state.getStatus().name(),
        "playerTurn",
        state.isPlayerTurn(),
        "activeUnitBattleId",
        state.getActiveUnitBattleId() != null ? state.getActiveUnitBattleId() : -999,
        "playerUnits",
        state.getPlayerUnits().stream().map(this::unitDto).toList(),
        "enemyUnits",
        state.getEnemyUnits().stream().map(this::unitDto).toList(),
        "battleLog",
        state.getBattleLog(),
        "pvp",
        state.isPvp(),
        "hpSnapshots",
        state.getHpSnapshots(),
        "manaSnapshots",
        state.getManaSnapshots());
  }

  /**
   * Converts a {@link BattleUnit} into a plain map suitable for JSON serialisation.
   *
   * @param u the {@link BattleUnit} to convert
   * @return a map representation of the unit's current state
   */
  private Map<String, Object> unitDto(BattleUnit u) {
    HeroSnapshot h = u.getHero();
    Map<String, Object> map = new HashMap<>();
    map.put("battleId", u.getBattleId());
    map.put("enemy", u.isEnemy());
    map.put("alive", u.isAlive());
    map.put("name", h.getName());
    map.put("level", h.getLevel());
    map.put("health", h.getHealth());
    map.put("maxHealth", h.getMaxHealth());
    map.put("mana", h.getMana());
    map.put("maxMana", h.getMaxMana());
    map.put("attack", h.getAttack());
    map.put("defense", h.getDefense());
    map.put(
        "startingClass", h.getStartingClass() != null ? h.getStartingClass().name() : "WARRIOR");
    map.put("primaryClass", h.getPrimaryClass() != null ? h.getPrimaryClass().name() : "");
    map.put("hybridClass", h.getHybridClass() != null ? h.getHybridClass().name() : "");
    return map;
  }
}
