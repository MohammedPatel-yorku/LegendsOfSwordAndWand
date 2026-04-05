package com.university.project.legendsofswordandwand.battle.enemy;

import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Component;

/**
 * Generates a randomised enemy party scaled to the player's cumulative level.
 *
 * <p>Each enemy type has a distinct {@link Archetype} affecting stats and an {@link EnemyBehaviour}
 * that drives AI targeting logic in {@code BattleServiceImpl}. By carrying the behavior alongside
 * the generated {@link Hero}, the battle service never needs to re-derive it from a name string.
 */
@Component
public class EnemyGenerator {

  private final Random random = new Random();

  /**
   * Pairs a generated enemy {@link Hero} with its {@link EnemyBehaviour} archetype. Returned by
   * {@link #generate} so callers can build {@link BattleUnit}s with the correct behavior without
   * coupling to the hero's display name.
   *
   * @param hero the generated enemy hero
   * @param behaviour the AI behaviour archetype for this enemy
   */
  public record EnemyEntry(Hero hero, EnemyBehaviour behaviour) {}

  /**
   * Enemy stat archetypes. Each defines base stats and per-level scaling.
   *
   * <ul>
   *   <li>GLASS_CANNON — high attack, low defense, low HP (hits hard, dies fast)
   *   <li>TANK — low attack, high defense, high HP (hard to kill, weak hits)
   *   <li>BALANCED — moderate everything (all-rounder)
   *   <li>SWIFT — above-average attack, very low defense, medium HP (aggressive)
   *   <li>BRUTE — high attack, medium defense, high HP (threatening)
   * </ul>
   */
  private enum Archetype {
    GLASS_CANNON("glass cannon", 7, 3, 0, 0, 35, 12),
    TANK("tank", 3, 2, 2, 2, 90, 25),
    BALANCED("balanced", 6, 2, 1, 1, 50, 18),
    SWIFT("swift", 7, 3, 0, 1, 45, 15),
    BRUTE("brute", 7, 3, 2, 1, 60, 22);

    final String label;
    final int atkBase, atkScale, defBase, defScale, hpBase, hpScale;

    Archetype(
        String label,
        int atkBase,
        int atkScale,
        int defBase,
        int defScale,
        int hpBase,
        int hpScale) {
      this.label = label;
      this.atkBase = atkBase;
      this.atkScale = atkScale;
      this.defBase = defBase;
      this.defScale = defScale;
      this.hpBase = hpBase;
      this.hpScale = hpScale;
    }

    /**
     * Returns the attack stat for a unit of the given level.
     *
     * @param level the unit's level
     * @return the computed attack value
     */
    int attack(int level) {
      return atkBase + (level - 1) * atkScale;
    }

    /**
     * Returns the defense stat for a unit of the given level.
     *
     * @param level the unit's level
     * @return the computed defense value
     */
    int defense(int level) {
      return defBase + (level - 1) * defScale;
    }

    /**
     * Returns the maximum health for a unit of the given level.
     *
     * @param level the unit's level
     * @return the computed HP value
     */
    int hp(int level) {
      return hpBase + (level - 1) * hpScale;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  /**
   * Each enemy name is pinned to a stat {@link Archetype} and an {@link EnemyBehaviour} so the same
   * name always feels and behaves the same, and the behavior is explicit rather than re-derived
   * from a name string at battle time.
   */
  private enum EnemyType {
    GOBLIN("Goblin", Archetype.SWIFT, EnemyBehaviour.SWIFT),
    ORC("Orc", Archetype.BRUTE, EnemyBehaviour.BRUTE),
    TROLL("Troll", Archetype.TANK, EnemyBehaviour.TANK),
    BANDIT("Bandit", Archetype.BALANCED, EnemyBehaviour.BALANCED),
    SKELETON("Skeleton", Archetype.GLASS_CANNON, EnemyBehaviour.GLASS_CANNON),
    DARK_KNIGHT("Dark Knight", Archetype.BRUTE, EnemyBehaviour.BRUTE),
    WITCH("Witch", Archetype.GLASS_CANNON, EnemyBehaviour.GLASS_CANNON),
    VAMPIRE("Vampire", Archetype.SWIFT, EnemyBehaviour.SWIFT),
    WYVERN("Wyvern", Archetype.BALANCED, EnemyBehaviour.BALANCED),
    SHADOW("Shadow", Archetype.GLASS_CANNON, EnemyBehaviour.GLASS_CANNON);

    final String name;
    final Archetype archetype;
    final EnemyBehaviour behaviour;

    EnemyType(String name, Archetype archetype, EnemyBehaviour behaviour) {
      this.name = name;
      this.archetype = archetype;
      this.behaviour = behaviour;
    }
  }

  /**
   * Generates a randomised enemy party scaled to the player's cumulative level and party size.
   *
   * <p>Each entry in the returned list pairs a fully initialised enemy {@link Hero} with its {@link
   * EnemyBehaviour} archetype. The behavior must be stored on the {@link BattleUnit} so that combat
   * AI can dispatch on it directly without re-deriving it from the name string.
   *
   * <p>Enemy count is at least {@code min(2, playerPartySize)} to avoid single-enemy fights against
   * larger parties. Individual enemy levels are capped at the player's average hero level plus 2.
   * The total cumulative level stays within a ±20 % variance band of the player's cumulative level,
   * slightly favouring the enemies.
   *
   * @param playerCumulativeLevel the sum of all player hero levels
   * @param playerPartySize the number of heroes in the player's party
   * @return a list of {@link EnemyEntry} records, each holding a hero and its behavior
   */
  public List<EnemyEntry> generate(int playerCumulativeLevel, int playerPartySize) {

    int minCount = Math.min(2, playerPartySize);
    int maxCount = Math.min(5, playerPartySize + 1);
    int count = minCount + random.nextInt(Math.max(1, maxCount - minCount + 1));

    int target = Math.max(playerCumulativeLevel + (playerCumulativeLevel / 5), count);
    int variance = Math.max(1, target / 5);
    int minTarget = Math.max(count, target - variance);
    int maxTarget = target + variance;
    int targetCumulativeLevel = minTarget + random.nextInt(maxTarget - minTarget + 1);

    int avgPartyLevel = Math.max(1, playerCumulativeLevel / Math.max(1, playerPartySize));
    int maxIndividualLevel = Math.max(2, avgPartyLevel + 2);
    int[] levels = distributeLevels(count, targetCumulativeLevel, maxIndividualLevel);

    List<EnemyEntry> entries = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      entries.add(buildEnemy(levels[i]));
    }
    return entries;
  }

  /**
   * Distributes a target cumulative level across a fixed number of enemies.
   *
   * <p>Each enemy starts at level 1. Remaining levels are randomly assigned one at a time, capped
   * per enemy at {@code maxIndividualLevel} to prevent single overpowered enemies.
   *
   * @param count the number of enemies
   * @param targetCumulativeLevel the total level sum to distribute
   * @param maxIndividualLevel the maximum level any single enemy may reach
   * @return an array of individual enemy levels
   */
  private int[] distributeLevels(int count, int targetCumulativeLevel, int maxIndividualLevel) {
    int[] levels = new int[count];
    Arrays.fill(levels, 1);
    int remaining = targetCumulativeLevel - count;

    while (remaining > 0) {
      List<Integer> eligible = new ArrayList<>();
      for (int i = 0; i < count; i++) {
        if (levels[i] < maxIndividualLevel) eligible.add(i);
      }
      if (eligible.isEmpty()) break;
      int idx = eligible.get(random.nextInt(eligible.size()));
      levels[idx]++;
      remaining--;
    }
    return levels;
  }

  /**
   * Builds a single enemy at the given level with a randomly chosen {@link EnemyType}. Returns an
   * {@link EnemyEntry} pairing the hero with its behavior archetype.
   *
   * @param level the level to build the enemy at
   * @return an {@link EnemyEntry} containing a fully initialised enemy hero and its behavior
   */
  private EnemyEntry buildEnemy(int level) {
    EnemyType type = EnemyType.values()[random.nextInt(EnemyType.values().length)];
    Archetype a = type.archetype;

    Hero enemy =
        Hero.builder()
            .name(type.name)
            .startingClass(HeroClass.values()[random.nextInt(HeroClass.values().length)])
            .party(null)
            .build();

    enemy.setLevel(level);
    enemy.setAttack(a.attack(level));
    enemy.setDefense(a.defense(level));
    enemy.setHealth(a.hp(level));
    enemy.setMaxHealth(a.hp(level));
    enemy.setMana(0);
    enemy.setMaxMana(0);

    return new EnemyEntry(enemy, type.behaviour);
  }
}
