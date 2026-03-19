package com.university.project.legendsofswordandwand.battle;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Generates a randomized enemy party scaled to the player's cumulative level.
 * Each enemy type has a distinct archetype affecting their attack, defense and HP
 * within the spec's constraint that stats scale with level and keep battles fair.
 */
@Component
public class EnemyGenerator {

    private final Random random = new Random();

    /**
     * Enemy archetypes. Each defines base stats and per-level scaling.
     * Archetypes deliberately feel different in combat:
     *   GLASS_CANNON  — high attack, low defense, low HP (hits hard, dies fast)
     *   TANK          — low attack, high defense, high HP (hard to kill, weak hits)
     *   BALANCED      — moderate everything (all-rounder)
     *   SWIFT         — above-average attack, very low defense, medium HP (aggressive)
     *   BRUTE         — high attack, medium defense, high HP (threatening)
     */
    private enum Archetype {
        //                    name           atkBase atkScale defBase defScale hpBase hpScale
        GLASS_CANNON("glass cannon",         8,      2,       1,      0,       45,    10),
        TANK        ("tank",                 4,      1,       5,      2,       90,    20),
        BALANCED    ("balanced",             6,      2,       2,      1,       60,    15),
        SWIFT       ("swift",                7,      2,       1,      1,       55,    12),
        BRUTE       ("brute",                7,      2,       3,      1,       75,    18);

        final String label;
        final int atkBase, atkScale, defBase, defScale, hpBase, hpScale;

        Archetype(String label,
                  int atkBase, int atkScale,
                  int defBase, int defScale,
                  int hpBase,  int hpScale) {
            this.label    = label;
            this.atkBase  = atkBase;  this.atkScale  = atkScale;
            this.defBase  = defBase;  this.defScale  = defScale;
            this.hpBase   = hpBase;   this.hpScale   = hpScale;
        }

        int attack(int level)  { return atkBase + (level - 1) * atkScale; }
        int defense(int level) { return defBase + (level - 1) * defScale; }
        int hp(int level)      { return hpBase  + (level - 1) * hpScale;  }
    }

    /** Each enemy name is pinned to an archetype so the same name always feels the same. */
    private enum EnemyType {
        GOBLIN    ("Goblin",      Archetype.SWIFT),
        ORC       ("Orc",         Archetype.BRUTE),
        TROLL     ("Troll",       Archetype.TANK),
        BANDIT    ("Bandit",      Archetype.BALANCED),
        SKELETON  ("Skeleton",    Archetype.GLASS_CANNON),
        DARK_KNIGHT("Dark Knight",Archetype.BRUTE),
        WITCH     ("Witch",       Archetype.GLASS_CANNON),
        VAMPIRE   ("Vampire",     Archetype.SWIFT),
        WYVERN    ("Wyvern",      Archetype.BALANCED),
        SHADOW    ("Shadow",      Archetype.GLASS_CANNON);

        final String name;
        final Archetype archetype;

        EnemyType(String name, Archetype archetype) {
            this.name      = name;
            this.archetype = archetype;
        }
    }

    public List<Hero> generate(int playerCumulativeLevel, int playerPartySize) {

        int count = 1 + random.nextInt(Math.min(5, playerPartySize + 1));

        int maxCumulativeLevel = Math.max(count, playerCumulativeLevel);
        int rawMin = maxCumulativeLevel - 10;
        int minCumulativeLevel = Math.max(count, rawMin);
        maxCumulativeLevel = Math.max(minCumulativeLevel, maxCumulativeLevel);
        int targetCumulativeLevel = minCumulativeLevel
                + random.nextInt(maxCumulativeLevel - minCumulativeLevel + 1);

        int[] levels = distributeLevels(count, targetCumulativeLevel);

        List<Hero> enemies = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            enemies.add(buildEnemy(levels[i]));
        }

        return enemies;
    }

    private int[] distributeLevels(int count, int targetCumulativeLevel) {
        int[] levels = new int[count];
        Arrays.fill(levels, 1);
        int remaining = targetCumulativeLevel - count;
        for (int i = 0; i < remaining; i++) {
            levels[random.nextInt(count)]++;
        }
        return levels;
    }

    private Hero buildEnemy(int level) {

        EnemyType type = EnemyType.values()[random.nextInt(EnemyType.values().length)];
        Archetype a = type.archetype;

        Hero enemy = Hero.builder()
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