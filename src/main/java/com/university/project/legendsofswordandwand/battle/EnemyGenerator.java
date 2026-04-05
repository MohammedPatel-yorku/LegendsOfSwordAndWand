package com.university.project.legendsofswordandwand.battle;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Component;

/**
 * Generates a randomized enemy party scaled to the player's cumulative level. Each enemy type has a
 * distinct archetype affecting their attack, defense and HP within the spec's constraint that stats
 * scale with level and keep battles fair.
 */
@Component
public class EnemyGenerator {

  private final Random random = new Random();

  /**
   * Enemy archetypes. Each defines base stats and per-level scaling. Archetypes deliberately feel
   * different in combat: GLASS_CANNON — high attack, low defense, low HP (hits hard, dies fast)
   * TANK — low attack, high defense, high HP (hard to kill, weak hits) BALANCED — moderate
   * everything (all-rounder) SWIFT — above-average attack, very low defense, medium HP (aggressive)
   * BRUTE — high attack, medium defense, high HP (threatening)
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

  /** Each enemy name is pinned to an archetype so the same name always feels the same. */
  private enum EnemyType {
    GOBLIN("Goblin", Archetype.SWIFT),
    ORC("Orc", Archetype.BRUTE),
    TROLL("Troll", Archetype.TANK),
    BANDIT("Bandit", Archetype.BALANCED),
    SKELETON("Skeleton", Archetype.GLASS_CANNON),
    DARK_KNIGHT("Dark Knight", Archetype.BRUTE),
    WITCH("Witch", Archetype.GLASS_CANNON),
    VAMPIRE("Vampire", Archetype.SWIFT),
    WYVERN("Wyvern", Archetype.BALANCED),
    SHADOW("Shadow", Archetype.GLASS_CANNON);

    final String name;
    final Archetype archetype;

    EnemyType(String name, Archetype archetype) {
      this.name = name;
      this.archetype = archetype;
    }
  }

  /**
   * Generates a randomized enemy party scaled to the player's cumulative level and party size.
   *
   * <p>Enemy count is at least {@code min(2, playerPartySize)} to avoid single-enemy fights against
   * larger parties. Individual enemy levels are capped at the player's average hero level plus 2,
   * preventing any single enemy from being dramatically overleveled. The total cumulative level
   * stays within a ±20% variance band of the player's cumulative level, slightly favouring the
   * enemies.
   *
   * @param playerCumulativeLevel the sum of all player hero levels
   * @param playerPartySize the number of heroes in the player's party
   * @return a list of generated enemy {@link Hero} instances
   */
  public List<Hero> generate(int playerCumulativeLevel, int playerPartySize) {

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

    List<Hero> enemies = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      enemies.add(buildEnemy(levels[i]));
    }
    return enemies;
  }

  /**
   * Distributes a target cumulative level across a fixed number of enemies.
   *
   * <p>Each enemy starts at level 1. Remaining levels are randomly assigned one at a time, capped
   * per enemy at {@code maxIndividualLevel} to prevent single overpowered enemies.
   *
   * @param count the number of enemies to distribute levels across
   * @param targetCumulativeLevel the total level sum to distribute
   * @param maxIndividualLevel the maximum level any single enemy can reach
   * @return an array of individual enemy levels
   */
  private int[] distributeLevels(int count, int targetCumulativeLevel, int maxIndividualLevel) {
    int[] levels = new int[count];
    Arrays.fill(levels, 1);

    int remaining = targetCumulativeLevel - count;

    while (remaining > 0) {
      // Find enemies that still have room to grow
      List<Integer> eligible = new ArrayList<>();
      for (int i = 0; i < count; i++) {
        if (levels[i] < maxIndividualLevel) eligible.add(i);
      }
      // If no enemy can grow further, stop
      if (eligible.isEmpty()) break;

      int idx = eligible.get(random.nextInt(eligible.size()));
      levels[idx]++;
      remaining--;
    }

    return levels;
  }

  /**
   * Builds a single enemy {@link Hero} of the given level with a randomly chosen {@link EnemyType}
   * and its associated {@link Archetype} stats.
   *
   * @param level the level to build the enemy at
   * @return a fully initialised enemy {@link Hero}
   */
  private Hero buildEnemy(int level) {
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

    return enemy;
  }
}
