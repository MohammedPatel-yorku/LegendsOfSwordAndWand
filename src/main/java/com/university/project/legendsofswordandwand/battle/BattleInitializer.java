package com.university.project.legendsofswordandwand.battle;

import com.university.project.legendsofswordandwand.model.enums.BattleStatus;
import java.util.List;

/**
 * Template Method pattern — defines the skeleton for initializing any battle.
 * Subclasses provide the units and post-battle logic specific to PvE or PvP.
 */
public abstract class BattleInitializer {

    /** Build the list of player-side BattleUnits. */
    protected abstract List<BattleUnit> buildPlayerUnits();

    /** Build the list of enemy-side BattleUnits. */
    protected abstract List<BattleUnit> buildEnemyUnits();

    /** Called after the battle ends — update DB, award rewards, etc. */
    public abstract void onBattleEnd(BattleState state);

    /** Template method — fixed skeleton, steps filled in by subclasses. */
    public final BattleState initialize() {

        BattleState state = new BattleState();

        state.getPlayerUnits().addAll(buildPlayerUnits());
        state.getEnemyUnits().addAll(buildEnemyUnits());

        List<BattleUnit> all = new java.util.ArrayList<>();
        all.addAll(state.getPlayerUnits());
        all.addAll(state.getEnemyUnits());
        all.sort((a, b) -> {
            int lvl = b.getHero().getLevel() - a.getHero().getLevel();
            return lvl != 0 ? lvl : b.getHero().getAttack() - a.getHero().getAttack();
        });
        all.forEach(u -> state.getTurnQueue().add(u.getBattleId()));

        Long first = state.getTurnQueue().pollFirst();
        state.setActiveUnitBattleId(first);
        BattleUnit firstUnit = state.findUnit(first);
        state.setPlayerTurn(firstUnit != null && !firstUnit.isEnemy());
        state.setStatus(BattleStatus.IN_PROGRESS);

        return state;
    }
}