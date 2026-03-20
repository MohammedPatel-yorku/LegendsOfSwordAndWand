package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.HeroSnapshot;
import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.ActionType;
import com.university.project.legendsofswordandwand.model.enums.BattleStatus;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.service.battle.IBattleService;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignProgressService;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignService;
import com.university.project.legendsofswordandwand.service.hero.IHeroService;
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

@Controller
@RequestMapping("/battle")
@RequiredArgsConstructor
public class BattleController {

  private static final String SESSION_KEY = "battleState";
  private static final String LAST_RESULT_KEY = "lastBattleResult";

  private final IBattleService battleService;
  private final ICampaignService campaignService;
  private final ICampaignProgressService campaignProgressService;
  private final IHeroService heroService;

  @GetMapping
  public String battlePage(Authentication authentication, HttpSession session) {
    if (authentication == null) return "redirect:/login";
    try {
      BattleState state = (BattleState) session.getAttribute(SESSION_KEY);
      if (state == null || state.isOver()) {
        var campaign = campaignService.getActiveCampaign(authentication.getName());
        int cumulativeLevel = campaignService.getPartyCumulativeLevel(authentication.getName());
        state = battleService.initializePvEBattle(campaign.getId(), cumulativeLevel);

        int safetyLimit = 50;
        while (!state.isOver() && !state.isPlayerTurn() && safetyLimit-- > 0) {
          state = battleService.executeEnemyTurn(state);
        }
        if (!state.isOver() && !state.isPlayerTurn()) {
          state.setStatus(battleService.checkBattleStatus(state));
        }
        session.setAttribute(SESSION_KEY, state);
      }
    } catch (Exception e) {
      return "redirect:/campaign";
    }
    return "battle/battle";
  }

  @GetMapping("/state")
  @ResponseBody
  public ResponseEntity<?> getState(Authentication authentication, HttpSession session) {
    if (authentication == null) return ResponseEntity.status(401).build();
    BattleState state = (BattleState) session.getAttribute(SESSION_KEY);
    if (state == null) return ResponseEntity.status(404).build();
    return ResponseEntity.ok(toDto(state));
  }

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

      int safetyLimit = 50;
      while (!state.isOver() && !state.isPlayerTurn() && safetyLimit-- > 0) {
        state = battleService.executeEnemyTurn(state);
      }
      if (!state.isOver() && !state.isPlayerTurn()) {
        state.setStatus(battleService.checkBattleStatus(state));
      }
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } finally {
      session.setAttribute(SESSION_KEY, state);
    }

    return ResponseEntity.ok(toDto(state));
  }

  @GetMapping("/result")
  public String result(Authentication authentication, Model model, HttpSession session) {
    if (authentication == null) return "redirect:/login";

    BattleState state = (BattleState) session.getAttribute(SESSION_KEY);
    if (state == null || !state.isOver()) return "redirect:/battle";

    try {

      boolean rewardsAlreadyGiven = Boolean.TRUE.equals(session.getAttribute("rewardsGiven"));
      if (!rewardsAlreadyGiven && state.getStatus() == BattleStatus.PLAYER_WIN) {
        Map<String, Object> rewards = battleService.awardBattleRewards(state);
        model.addAttribute("rewardGold", rewards.get("gold"));
        model.addAttribute("rewardRecipients", rewards.get("recipients"));
        session.setAttribute("rewardsGiven", true);
      } else if (state.getStatus() == BattleStatus.PLAYER_WIN) {
        // Rewards already given, just show level up panel
        model.addAttribute("rewardGold", 0);
        model.addAttribute("rewardRecipients", List.of());
      }

      if (!rewardsAlreadyGiven && state.getStatus() == BattleStatus.PLAYER_LOSE) {
        battleService.applyBattleLoss(state);
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
      if (state.isPvp() && !rewardsAlreadyGiven) {
        battleService.updatePvPResult(state);
        session.setAttribute("rewardsGiven", true);
      }
      session.setAttribute(LAST_RESULT_KEY, state.getStatus().name());
    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
    }

    return "battle/result";
  }

  @PostMapping("/continue")
  public String continueCampaign(Authentication authentication, HttpSession session) {
    if (authentication == null) return "redirect:/login";

    BattleState state = (BattleState) session.getAttribute(SESSION_KEY);
    boolean wasPvp = state != null && state.isPvp();

    session.removeAttribute(SESSION_KEY);
    session.removeAttribute("rewardsGiven");

    if (wasPvp) {
      session.removeAttribute(LAST_RESULT_KEY);
      return "redirect:/pvp";
    }

    try {
      String lastResult = (String) session.getAttribute(LAST_RESULT_KEY);

      if (campaignProgressService.isCampaignComplete(authentication.getName())) {
        session.removeAttribute(LAST_RESULT_KEY);
        campaignService.completeCampaign(authentication.getName());
        return "redirect:/campaign/complete";
      }

      if ("PLAYER_LOSE".equals(lastResult)) {
        Campaign campaign = campaignService.getActiveCampaign(authentication.getName());
        if (campaign.isHasVisitedInn()) {
          return "redirect:/inn";
        } else {
          // No inn visited yet — just clear the pending room and return to campaign
          session.removeAttribute(LAST_RESULT_KEY);
          campaignService.abandonCampaign(authentication.getName());
          return "redirect:/dashboard";
        }
      }

      session.removeAttribute(LAST_RESULT_KEY);
      campaignProgressService.clearRoomPending(authentication.getName());
    } catch (Exception ignored) {
    }
    return "redirect:/campaign";
  }

  private Map<String, Object> toDto(BattleState state) {
    return Map.of(
        "over", state.isOver(),
        "status", state.getStatus().name(),
        "playerTurn", state.isPlayerTurn(),
        "activeUnitBattleId",
            state.getActiveUnitBattleId() != null ? state.getActiveUnitBattleId() : -999,
        "playerUnits", state.getPlayerUnits().stream().map(this::unitDto).toList(),
        "enemyUnits", state.getEnemyUnits().stream().map(this::unitDto).toList(),
        "battleLog", state.getBattleLog());
  }

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
