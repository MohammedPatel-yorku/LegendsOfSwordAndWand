package com.university.project.legendsofswordandwand.battle.enemy;

/**
 * Represents the five AI behaviour archetypes available to enemy units in battle.
 *
 * <p>Decouples enemy targeting logic in {@code BattleServiceImpl} from the enemy's display name
 * string, eliminating the fragile name-based {@code switch} statement. Each constant corresponds to
 * a distinct combat personality defined in {@code EnemyGenerator.EnemyType}.
 */
public enum EnemyBehaviour {

  /**
   * Attacks the lowest-HP target every turn. High damage output, low survivability — hits hard and
   * dies fast. Example enemies: Skeleton, Witch, Shadow.
   */
  GLASS_CANNON,

  /**
   * Targets the highest-attack hero 60 % of the time; picks a random target otherwise. Prioritises
   * eliminating the biggest offensive threat. Example enemies: Orc, Dark Knight.
   */
  BRUTE,

  /**
   * Targets the lowest-defense hero 75 % of the time; waits otherwise. Aggressively exploits the
   * weakest armour in the party. Example enemies: Goblin, Vampire.
   */
  SWIFT,

  /**
   * Defends when below 25 % HP (40 % chance); otherwise targets the highest-HP hero or a random
   * target. Trades burst damage for durability. Example enemy: Troll.
   */
  TANK,

  /**
   * Attacks a random target 85 % of the time; defends otherwise. General-purpose behaviour with no
   * specific targeting priority. Example enemies: Bandit, Wyvern.
   */
  BALANCED
}
