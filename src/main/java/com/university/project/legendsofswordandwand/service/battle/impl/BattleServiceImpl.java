package com.university.project.legendsofswordandwand.service.battle.impl;

import com.university.project.legendsofswordandwand.battle.*;
import com.university.project.legendsofswordandwand.battle.ability.AbilityHelper;
import com.university.project.legendsofswordandwand.battle.initializer.PvEBattleInitializer;
import com.university.project.legendsofswordandwand.battle.initializer.PvPBattleInitializer;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.enums.ActionType;
import com.university.project.legendsofswordandwand.model.enums.BattleStatus;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.battle.IBattleService;
import com.university.project.legendsofswordandwand.service.hero.IHeroService;
import com.university.project.legendsofswordandwand.service.party.IPartyManagementService;
import jakarta.transaction.Transactional;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link IBattleService}, managing the full lifecycle of a battle
 * including initialisation, turn processing, enemy AI, reward distribution, and loss penalties.
 */
@Service
@Transactional
@RequiredArgsConstructor
class BattleServiceImpl implements IBattleService {

    // ── Smell 2 fix: named constants replacing magic number literals ───────────
    private static final int    XP_PER_ENEMY_LEVEL         = 50;
    private static final int    GOLD_PER_ENEMY_LEVEL        = 75;
    private static final double XP_LOSS_PENALTY_FRACTION    = 0.30;
    private static final double GOLD_LOSS_PENALTY_FRACTION  = 0.10;

    private final DamageCalculator damageCalculator;
    private final AbilityExecutor abilityExecutor;
    private final EnemyGenerator enemyGenerator;
    private final IHeroService heroService;
    private final IPartyManagementService partyManagementService;
    private final PartyRepository partyRepository;
    private final UserRepository userRepository;
    // Smell 3 fix: injected so applyBattleLoss() can call getExpStepForLevel()
    private final HeroStatCalculator heroStatCalculator;
    private final Random random = new Random();

    /**
     * Initialises a PvE battle for the given campaign, generating a scaled enemy party and building
     * the initial turn queue.
     *
     * @param campaignId the ID of the campaign this battle belongs to
     * @param playerCumulativeLevel the sum of all player hero levels, used to scale enemies
     * @return the fully initialised {@link BattleState}
     */
    @Override
    public BattleState initializePvEBattle(Long campaignId, int playerCumulativeLevel) {
        PvEBattleInitializer initializer =
                new PvEBattleInitializer(
                        campaignId, playerCumulativeLevel, partyManagementService, enemyGenerator);
        BattleState state = initializer.initialize();
        state.setCampaignId(campaignId);
        return state;
    }

    /**
     * Initialises a PvP battle for the invitation sender and receiver.
     *
     * @param senderPartyId the ID of the party belonging to the sender
     * @param receiverPartyId the ID of the party belonging to the receiver
     * @return the fully initialised {@link BattleState}
     */
    @Override
    public BattleState initializePvPBattle(
            Long senderPartyId, Long receiverPartyId, Long invitationId) {

        Party senderParty =
                partyRepository
                        .findById(senderPartyId)
                        .orElseThrow(() -> new RuntimeException("Sender party not found"));
        Party receiverParty =
                partyRepository
                        .findById(receiverPartyId)
                        .orElseThrow(() -> new RuntimeException("Receiver party not found"));

        PvPBattleInitializer initializer =
                new PvPBattleInitializer(senderParty, receiverParty, invitationId, userRepository);
        BattleState state = initializer.initialize();
        state.setPvp(true);
        state.setPvpInvitationId(invitationId);
        state.setPvpSenderUsername(senderParty.getOwner().getUsername());
        state.setPvpReceiverUsername(receiverParty.getOwner().getUsername());

        return state;
    }

    /**
     * Executes a player action for the currently active unit and advances the turn.
     *
     * @param state the current {@link BattleState}
     * @param actionType the {@link ActionType} the player wishes to perform
     * @param targetBattleId the battle ID of the target unit, if required by the action
     * @param abilityIndex the ability slot index for {@code CAST} actions
     * @return the updated {@link BattleState}
     */
    @Override
    public BattleState executePlayerAction(
            BattleState state, ActionType actionType, Long targetBattleId, Integer abilityIndex) {

        if (state.isOver()) return state;
        if (!state.isPvp() && !state.isPlayerTurn()) return state;

        BattleUnit actor = state.getActiveUnit();
        if (actor == null || !actor.isAlive()) return advanceTurn(state);

        state.log(
                "► "
                        + actor.getHero().getName()
                        + " [HP:"
                        + actor.getHero().getHealth()
                        + "] acts: "
                        + actionType);

        BattleUnit target = targetBattleId != null ? state.findUnit(targetBattleId) : null;

        switch (actionType) {
            case ATTACK -> {
                if (target != null) executeAttack(actor, target, state);
            }
            case DEFEND -> {
                executeDefend(actor.getHero());
                state.log("  " + actor.getHero().getName() + " defends (+10 HP, +5 MP)");
            }
            case WAIT -> {
                state.log("  " + actor.getHero().getName() + " waits");
                state.getTurnQueue().addLast(actor.getBattleId());
                state.setActiveUnitBattleId(null);
                state.setStatus(checkBattleStatus(state));
                return advanceTurn(state);
            }
            case CAST -> {
                List<BattleUnit> allies =
                        actor.isEnemy() ? state.getLivingEnemyHeroes() : state.getLivingPlayerHeroes();
                List<BattleUnit> enemies =
                        actor.isEnemy() ? state.getLivingPlayerHeroes() : state.getLivingEnemyHeroes();
                abilityExecutor.executeAbility(
                        actor, target, allies, enemies, state, abilityIndex != null ? abilityIndex : 0);
            }
        }

        state.setStatus(checkBattleStatus(state));
        if (!state.isOver()) advanceTurn(state);
        return state;
    }

    /**
     * Executes a single enemy turn for the currently active enemy unit and advances the turn.
     *
     * @param state the current {@link BattleState}
     * @return the updated {@link BattleState}
     */
    @Override
    public BattleState executeEnemyTurn(BattleState state) {
        if (state.isOver() || state.isPlayerTurn()) return state;

        BattleUnit actor = state.getActiveUnit();
        if (actor == null || !actor.isAlive()) return advanceTurn(state);

        if (state.isStunned(actor.getBattleId())) {
            state.tickStuns();
            return advanceTurn(state);
        }

        List<BattleUnit> targets = state.getLivingPlayerHeroes();
        if (!targets.isEmpty()) {
            decideEnemyAction(actor, targets, state);
        } else {
            executeDefend(actor.getHero());
            state.log("  " + actor.getHero().getName() + " defends");
        }

        state.setStatus(checkBattleStatus(state));
        if (!state.isOver()) advanceTurn(state);
        return state;
    }

    /**
     * Selects and executes an action for an enemy unit based on its name/archetype.
     *
     * @param actor the enemy {@link BattleUnit} taking the action
     * @param targets the list of living player {@link BattleUnit}s to target
     * @param state the current {@link BattleState}
     */
    private void decideEnemyAction(BattleUnit actor, List<BattleUnit> targets, BattleState state) {
        String name = actor.getHero().getName();

        switch (name) {
            case "Skeleton", "Witch", "Shadow" -> {
                BattleUnit target =
                        targets.stream()
                                .min(Comparator.comparingInt(u -> u.getHero().getHealth()))
                                .orElse(targets.get(0));
                executeAttack(actor, target, state);
            }
            case "Orc", "Dark Knight" -> {
                BattleUnit target =
                        random.nextInt(100) < 60
                                ? targets.stream()
                                .max(Comparator.comparingInt(u -> u.getHero().getAttack()))
                                .orElse(targets.get(0))
                                : targets.get(random.nextInt(targets.size()));
                executeAttack(actor, target, state);
            }
            case "Goblin", "Vampire" -> {
                if (random.nextInt(100) < 75) {
                    BattleUnit target =
                            targets.stream()
                                    .min(Comparator.comparingInt(u -> u.getHero().getDefense()))
                                    .orElse(targets.get(0));
                    executeAttack(actor, target, state);
                } else {
                    state.getTurnQueue().addLast(actor.getBattleId());
                    state.log("  " + actor.getHero().getName() + " waits");
                }
            }
            case "Troll" -> {
                boolean hurt = actor.getHero().getHealth() < actor.getHero().getMaxHealth() / 4;
                if (hurt && random.nextInt(100) < 40) {
                    executeDefend(actor.getHero());
                    state.log("  " + actor.getHero().getName() + " defends");
                } else {
                    BattleUnit target =
                            random.nextInt(100) < 50
                                    ? targets.stream()
                                    .max(Comparator.comparingInt(u -> u.getHero().getHealth()))
                                    .orElse(targets.get(0))
                                    : targets.get(random.nextInt(targets.size()));
                    executeAttack(actor, target, state);
                }
            }
            default -> {
                if (random.nextInt(100) < 85) {
                    BattleUnit target = targets.get(random.nextInt(targets.size()));
                    executeAttack(actor, target, state);
                } else {
                    executeDefend(actor.getHero());
                    state.log("  " + actor.getHero().getName() + " defends");
                }
            }
        }
    }

    /**
     * Checks and returns the current {@link BattleStatus} based on surviving units.
     *
     * @param state the current {@link BattleState}
     * @return {@code PLAYER_WIN}, {@code PLAYER_LOSE}, or {@code IN_PROGRESS}
     */
    @Override
    public BattleStatus checkBattleStatus(BattleState state) {
        if (state.getLivingEnemyHeroes().isEmpty()) return BattleStatus.PLAYER_WIN;
        if (state.getLivingPlayerHeroes().isEmpty()) return BattleStatus.PLAYER_LOSE;
        return BattleStatus.IN_PROGRESS;
    }

    /**
     * Awards XP and gold to surviving player heroes after a victory.
     *
     * @param state the current {@link BattleState}
     * @return a map containing {@code "gold"} (int) and {@code "recipients"} (list of reward strings)
     */
    @Override
    public Map<String, Object> awardBattleRewards(BattleState state) {
        List<BattleUnit> living = state.getLivingPlayerHeroes();
        if (living.isEmpty()) return Map.of("xpEach", 0, "gold", 0, "recipients", List.of());

        // Smell 2 fix: XP_PER_ENEMY_LEVEL replaces magic literal 50
        int totalXp = state.getEnemyUnits().stream()
                .mapToInt(u -> XP_PER_ENEMY_LEVEL * u.getHero().getLevel()).sum();
        int xpEach = totalXp / living.size();
        int remainder = totalXp % living.size();

        List<String> recipients = new ArrayList<>();
        for (int i = 0; i < living.size(); i++) {
            int xp = xpEach + (i == 0 ? remainder : 0);
            heroService.addExperience(living.get(i).getHero().getId(), xp);
            recipients.add(living.get(i).getHero().getName() + " +" + xp + " XP");
        }

        // Smell 2 fix: GOLD_PER_ENEMY_LEVEL replaces magic literal 75
        int gold = state.getEnemyUnits().stream()
                .mapToInt(u -> GOLD_PER_ENEMY_LEVEL * u.getHero().getLevel()).sum();
        if (state.getCampaignId() != null) {
            Party party = partyManagementService.getActiveParty(state.getCampaignId());
            partyManagementService.addGold(party.getId(), gold);
        }

        state
                .getPlayerUnits()
                .forEach(
                        u ->
                                heroService
                                        .findById(u.getHero().getId())
                                        .ifPresent(
                                                hero -> {
                                                    hero.setHealth(u.getHero().getHealth());
                                                    hero.setMana(u.getHero().getMana());
                                                    heroService.save(hero);
                                                }));

        Map<String, Object> rewards = new HashMap<>();
        rewards.put("gold", gold);
        rewards.put("recipients", recipients);
        return rewards;
    }

    /**
     * Applies penalties to all player heroes after a battle loss.
     *
     * @param state the current {@link BattleState}
     */
    @Override
    public void applyBattleLoss(BattleState state) {

        state
                .getPlayerUnits()
                .forEach(
                        u -> {
                            // Smell 3 fix: inlined formula replaced with heroStatCalculator.getExpStepForLevel()
                            int prevThreshold =
                                    u.getHero().getExperienceToNextLevel()
                                            - heroStatCalculator.getExpStepForLevel(u.getHero().getLevel());
                            int xpInCurrentLevel = Math.max(0, u.getHero().getExperience() - prevThreshold);
                            // Smell 2 fix: XP_LOSS_PENALTY_FRACTION replaces magic literal 0.30
                            int penalty = (int) (xpInCurrentLevel * XP_LOSS_PENALTY_FRACTION);
                            int newXp = Math.max(prevThreshold, u.getHero().getExperience() - penalty);
                            heroService
                                    .findById(u.getHero().getId())
                                    .ifPresent(
                                            hero -> {
                                                hero.setExperience(newXp);
                                                hero.setHealth(u.getHero().getHealth());
                                                hero.setMana(u.getHero().getMana());
                                                heroService.save(hero);
                                            });
                        });

        if (state.getCampaignId() != null) {
            Party party = partyManagementService.getActiveParty(state.getCampaignId());
            // Smell 2 fix: GOLD_LOSS_PENALTY_FRACTION replaces magic literal 0.10
            partyManagementService.deductGold(
                    party.getId(), (int) (party.getGold() * GOLD_LOSS_PENALTY_FRACTION));
        }
    }

    @Override
    public void updatePvPResult(BattleState state) {
        if (!state.isPvp() || !state.isOver()) return;

        boolean senderWon = state.getStatus() == BattleStatus.PLAYER_WIN;

        String winnerUsername =
                senderWon ? state.getPvpSenderUsername() : state.getPvpReceiverUsername();
        String loserUsername =
                senderWon ? state.getPvpReceiverUsername() : state.getPvpSenderUsername();

        userRepository
                .findByUsername(winnerUsername)
                .ifPresent(
                        u -> {
                            u.setPvpWins(u.getPvpWins() + 1);
                            userRepository.save(u);
                        });
        userRepository
                .findByUsername(loserUsername)
                .ifPresent(
                        u -> {
                            u.setPvpLosses(u.getPvpLosses() + 1);
                            userRepository.save(u);
                        });

        restorePartyHeroes(state.getPlayerUnits());
        restorePartyHeroes(state.getEnemyUnits());
    }

    private void restorePartyHeroes(List<BattleUnit> units) {
        units.forEach(
                u -> {
                    if (u.getHero().getId() == null) return;
                    heroService
                            .findById(u.getHero().getId())
                            .ifPresent(
                                    hero -> {
                                        hero.setHealth(hero.getMaxHealth());
                                        hero.setMana(hero.getMaxMana());
                                        heroService.save(hero);
                                    });
                });
    }

    private void executeAttack(BattleUnit attacker, BattleUnit defender, BattleState state) {
        int damage =
                damageCalculator.calculateDamage(
                        attacker.getHero().getAttack(), defender.getHero().getDefense());
        int hpBefore = defender.getHero().getHealth();
        int shieldBefore = Math.abs(state.getShield(defender.getBattleId()));
        AbilityHelper.applyDamage(attacker.getHero(), defender, damage, state);
        int actualDamage = hpBefore - defender.getHero().getHealth();

        state.log(
                "  "
                        + attacker.getHero().getName()
                        + " attacks "
                        + defender.getHero().getName()
                        + " for "
                        + damage
                        + " dmg → "
                        + defender.getHero().getHealth()
                        + " HP left"
                        + (shieldBefore > 0 && actualDamage < damage
                        ? " (shield absorbed " + (damage - actualDamage) + ")"
                        : ""));

        HybridClass hybrid = attacker.getHero().getHybridClass();
        if (hybrid == HybridClass.ROGUE) abilityExecutor.maybeSneak(attacker, defender, state);
        if (hybrid == HybridClass.WARLOCK) abilityExecutor.applyManaBurn(defender);
    }

    private void executeDefend(HeroSnapshot hero) {
        hero.setHealth(Math.min(hero.getMaxHealth(), hero.getHealth() + 10));
        hero.setMana(Math.min(hero.getMaxMana(), hero.getMana() + 5));
    }

    private BattleState advanceTurn(BattleState state) {
        Long next = state.getTurnQueue().pollFirst();

        while (next != null) {
            BattleUnit unit = state.findUnit(next);
            if (unit != null && unit.isAlive()) break;
            next = state.getTurnQueue().pollFirst();
        }

        if (next == null) {
            state.tickStuns();
            refillTurnQueue(state);
            next = state.getTurnQueue().pollFirst();
            while (next != null) {
                BattleUnit unit = state.findUnit(next);
                if (unit != null && unit.isAlive()) break;
                next = state.getTurnQueue().pollFirst();
            }
        }

        if (next != null) {
            state.setActiveUnitBattleId(next);
            state.setPlayerTurn(!state.getActiveUnit().isEnemy());
        }
        return state;
    }

    private void buildTurnQueue(BattleState state) {
        refillTurnQueue(state);
        state.setActiveUnitBattleId(state.getTurnQueue().pollFirst());
        state.setPlayerTurn(!state.getActiveUnit().isEnemy());
    }

    private void refillTurnQueue(BattleState state) {
        state.getTurnQueue().clear();

        List<BattleUnit> living = new ArrayList<>();
        living.addAll(state.getLivingPlayerHeroes());
        living.addAll(state.getLivingEnemyHeroes());
        living.sort(
                (a, b) -> {
                    int lvlDiff = b.getHero().getLevel() - a.getHero().getLevel();
                    return lvlDiff != 0 ? lvlDiff : b.getHero().getAttack() - a.getHero().getAttack();
                });
        living.forEach(u -> state.getTurnQueue().add(u.getBattleId()));
    }
}