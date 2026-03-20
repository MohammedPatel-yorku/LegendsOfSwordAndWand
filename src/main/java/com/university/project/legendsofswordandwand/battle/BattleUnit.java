package com.university.project.legendsofswordandwand.battle;

import java.io.Serializable;
import lombok.Getter;

/**
 * Represents a single combatant in a battle, wrapping a {@link HeroSnapshot}
 * with a unique battle ID and a flag indicating which side they are on.
 *
 * <p>Implements {@link Serializable} to support session persistence.
 */
@Getter
public class BattleUnit implements Serializable {

    /** The unique identifier assigned to this unit for the duration of the battle. */
    private final long battleId;

    /** The snapshot of the hero's stats and state for this battle unit. */
    private final HeroSnapshot hero;

    /** Whether this unit belongs to the enemy side. */
    private final boolean enemy;

    /**
     * Constructs a {@code BattleUnit} with the given battle ID, hero snapshot, and side flag.
     *
     * @param battleId the unique battle ID assigned to this unit
     * @param hero     the {@link HeroSnapshot} representing this unit's stats and state
     * @param enemy    {@code true} if this unit is an enemy, {@code false} if player-controlled
     */
    public BattleUnit(long battleId, HeroSnapshot hero, boolean enemy) {
        this.battleId = battleId;
        this.hero = hero;
        this.enemy = enemy;
    }

    /**
     * Returns {@code true} if this unit is still alive.
     *
     * @return {@code true} if the hero's current health is greater than {@code 0}
     */
    public boolean isAlive() {
        return hero.getHealth() > 0;
    }
}