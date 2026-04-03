package com.university.project.legendsofswordandwand.battle;

import com.university.project.legendsofswordandwand.battle.enemy.EnemyBehaviour;
import java.io.Serializable;
import lombok.Getter;

/**
 * Represents a single combatant in a battle, wrapping a {@link HeroSnapshot} with a unique battle
 * ID and a flag indicating which side they are on.
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
   * The AI behaviour archetype for this unit. {@code null} for player-controlled units; always set
   * for enemy units.
   */
  private final EnemyBehaviour behaviour;

  /**
   * Constructs a player-controlled {@code BattleUnit} with no AI behaviour.
   *
   * @param battleId the unique battle ID assigned to this unit
   * @param hero the {@link HeroSnapshot} representing this unit's stats and state
   * @param enemy {@code true} if this unit is an enemy; {@code false} if player-controlled
   */
  public BattleUnit(long battleId, HeroSnapshot hero, boolean enemy) {
    this(battleId, hero, enemy, null);
  }

  /**
   * Constructs a {@code BattleUnit} with a specified AI behaviour archetype. Use this constructor
   * for enemy units so their targeting logic is not derived from name strings.
   *
   * @param battleId the unique battle ID assigned to this unit
   * @param hero the {@link HeroSnapshot} representing this unit's stats and state
   * @param enemy {@code true} if this unit is an enemy; {@code false} if player-controlled
   * @param behaviour the {@link EnemyBehaviour} archetype for AI targeting; {@code null} for
   *     players
   */
  public BattleUnit(long battleId, HeroSnapshot hero, boolean enemy, EnemyBehaviour behaviour) {
    this.battleId = battleId;
    this.hero = hero;
    this.enemy = enemy;
    this.behaviour = behaviour;
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
