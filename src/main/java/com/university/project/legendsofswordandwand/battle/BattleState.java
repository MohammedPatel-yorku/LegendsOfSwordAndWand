package com.university.project.legendsofswordandwand.battle;

import com.university.project.legendsofswordandwand.model.enums.BattleStatus;
import java.io.Serializable;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the full mutable state of an ongoing battle.
 *
 * <p>Holds all units, the turn queue, battle log, shields, stun tracking,
 * and overall battle status. Implements {@link Serializable} to support
 * session persistence.
 */
@Getter
@Setter
public class BattleState implements Serializable {

    /** The list of all player-controlled {@link BattleUnit}s in this battle. */
    private List<BattleUnit> playerUnits = new ArrayList<>();

    /** The list of all enemy {@link BattleUnit}s in this battle. */
    private List<BattleUnit> enemyUnits = new ArrayList<>();

    /** The ordered queue of battle IDs determining the turn order. */
    private LinkedList<Long> turnQueue = new LinkedList<>();

    /** The battle ID of the unit whose turn is currently active. */
    private Long activeUnitBattleId;

    /** The running log of battle events, appended to by abilities and combat actions. */
    private List<String> battleLog = new ArrayList<>();

    /**
     * Appends a message to the battle log.
     *
     * @param message the message to log
     */
    public void log(String message) {
        battleLog.add(message);
    }

    /**
     * Maps each unit's battle ID to their current shield value.
     * Negative values indicate a fire shield.
     */
    private Map<Long, Integer> shields = new HashMap<>();

    /** Maps each unit's battle ID to the number of stun turns remaining. */
    private Map<Long, Integer> stunned = new HashMap<>();

    /** Whether it is currently the player's turn. */
    private boolean playerTurn;

    /** The current status of the battle. */
    private BattleStatus status = BattleStatus.IN_PROGRESS;

    /** The ID of the campaign this battle belongs to, if any. */
    private Long campaignId;

    /** Whether this battle is a PvP encounter. */
    private boolean pvp = false;

    /**
     * Returns {@code true} if the battle has concluded.
     *
     * @return {@code true} if the status is not {@link BattleStatus#IN_PROGRESS}
     */
    public boolean isOver() {
        return status != BattleStatus.IN_PROGRESS;
    }

    /**
     * Returns all player units that are still alive.
     *
     * @return a list of living player {@link BattleUnit}s
     */
    public List<BattleUnit> getLivingPlayerHeroes() {
        return playerUnits.stream().filter(BattleUnit::isAlive).toList();
    }

    /**
     * Returns all enemy units that are still alive.
     *
     * @return a list of living enemy {@link BattleUnit}s
     */
    public List<BattleUnit> getLivingEnemyHeroes() {
        return enemyUnits.stream().filter(BattleUnit::isAlive).toList();
    }

    /**
     * Finds and returns the {@link BattleUnit} with the given battle ID,
     * searching both player and enemy unit lists.
     *
     * @param battleId the battle ID to search for
     * @return the matching {@link BattleUnit}, or {@code null} if not found
     */
    public BattleUnit findUnit(long battleId) {
        for (BattleUnit unit : playerUnits) if (unit.getBattleId() == battleId) return unit;
        for (BattleUnit unit : enemyUnits) if (unit.getBattleId() == battleId) return unit;
        return null;
    }

    /**
     * Returns the {@link BattleUnit} whose turn is currently active.
     *
     * @return the active {@link BattleUnit}, or {@code null} if not found
     */
    public BattleUnit getActiveUnit() {
        return findUnit(activeUnitBattleId);
    }

    /**
     * Returns {@code true} if the unit with the given ID has at least one stun turn remaining.
     *
     * @param heroId the battle ID of the unit to check
     * @return {@code true} if the unit is stunned
     */
    public boolean isStunned(Long heroId) {
        return stunned.getOrDefault(heroId, 0) > 0;
    }

    /**
     * Applies a one-turn stun to the unit with the given battle ID.
     *
     * @param heroId the battle ID of the unit to stun
     */
    public void applyStun(Long heroId) {
        stunned.put(heroId, 1);
    }

    /**
     * Decrements the stun counter for all stunned units by one, floored at {@code 0}.
     * Should be called at the end of each turn.
     */
    public void tickStuns() {
        stunned.replaceAll((id, turns) -> Math.max(0, turns - 1));
    }

    /**
     * Returns the current shield value for the unit with the given battle ID.
     * A negative value indicates a fire shield.
     *
     * @param battleId the battle ID of the unit
     * @return the shield value, or {@code 0} if no shield is active
     */
    public int getShield(Long battleId) {
        return shields.getOrDefault(battleId, 0);
    }

    /**
     * Sets the shield value for the unit with the given battle ID.
     * Use a negative value to indicate a fire shield.
     *
     * @param battleId the battle ID of the unit
     * @param amount   the shield value to set
     */
    public void setShield(Long battleId, int amount) {
        shields.put(battleId, amount);
    }
}