package com.university.project.legendsofswordandwand.controller;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.HeroSnapshot;
import com.university.project.legendsofswordandwand.model.enums.ActionType;
import com.university.project.legendsofswordandwand.model.enums.BattleStatus;
import com.university.project.legendsofswordandwand.service.battle.IBattleService;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignProgressService;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignService;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
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

    private final IBattleService battleService;
    private final ICampaignService campaignService;
    private final ICampaignProgressService campaignProgressService;

    // ── Page load ─────────────────────────────────────────────────────────────

    @GetMapping
    public String battlePage(Authentication authentication, HttpSession session) {
        if (authentication == null) return "redirect:/login";
        try {
            BattleState state = (BattleState) session.getAttribute(SESSION_KEY);
            if (state == null || state.isOver()) {
                var campaign = campaignService.getActiveCampaign(authentication.getName());
                int cumulativeLevel = campaignService.getPartyCumulativeLevel(authentication.getName());
                state = battleService.initializePvEBattle(campaign.getId(), cumulativeLevel);

                // Auto-process any enemy turns that come first
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

    // ── JSON API ───────────────────────────────────────────────────────────────

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
            while (!state.isOver() && !state.isPlayerTurn()) {
                state = battleService.executeEnemyTurn(state);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } finally {
            session.setAttribute(SESSION_KEY, state);
        }

        return ResponseEntity.ok(toDto(state));
    }

    // ── Result page (stays Thymeleaf) ─────────────────────────────────────────

    @GetMapping("/result")
    public String result(Authentication authentication, Model model, HttpSession session) {
        if (authentication == null) return "redirect:/login";

        BattleState state = (BattleState) session.getAttribute(SESSION_KEY);
        if (state == null) return "redirect:/battle";

        try {
            if (state.getStatus() == BattleStatus.PLAYER_WIN) {
                battleService.awardBattleRewards(state);
            } else {
                battleService.applyBattleLoss(state);
            }
            model.addAttribute("status", state.getStatus());
            model.addAttribute("playerUnits", state.getPlayerUnits());
            model.addAttribute("enemyUnits", state.getEnemyUnits());
            boolean campaignDone = !state.isPvp()
                    && campaignProgressService.isCampaignComplete(authentication.getName());
            model.addAttribute("campaignDone", campaignDone);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        session.removeAttribute(SESSION_KEY);
        return "battle/result";
    }

    @PostMapping("/continue")
    public String continueCampaign(Authentication authentication) {
        if (authentication == null) return "redirect:/login";
        try {
            if (campaignProgressService.isCampaignComplete(authentication.getName())) {
                campaignService.completeCampaign(authentication.getName());
                return "redirect:/campaign/complete";
            }
            campaignProgressService.clearRoomPending(authentication.getName());
        } catch (Exception ignored) {}
        return "redirect:/campaign";
    }

    // ── DTO builder ────────────────────────────────────────────────────────────

    private Map<String, Object> toDto(BattleState state) {
        return Map.of(
                "over", state.isOver(),
                "status", state.getStatus().name(),
                "playerTurn", state.isPlayerTurn(),
                "activeUnitBattleId", state.getActiveUnitBattleId() != null ? state.getActiveUnitBattleId() : -999,
                "playerUnits", state.getPlayerUnits().stream().map(this::unitDto).toList(),
                "enemyUnits", state.getEnemyUnits().stream().map(this::unitDto).toList(),
                "battleLog", state.getBattleLog()
        );
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
        map.put("startingClass", h.getStartingClass() != null ? h.getStartingClass().name() : "WARRIOR");
        map.put("primaryClass", h.getPrimaryClass() != null ? h.getPrimaryClass().name() : "");
        return map;
    }
}