package com.university.project.legendsofswordandwand.battle;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class EnemyGenerator {

    private final Random random = new Random();
    private final String[] NAMES = {
            "Goblin", "Orc", "Troll", "Bandit", "Skeleton",
            "Dark Knight", "Witch", "Vampire", "Wyvern", "Shadow"
    };

    public List<Hero> generate(int playerCumulativeLevel, int playerPartySize) {

        int count = 1 + random.nextInt(Math.min(5, playerPartySize + 1));

        int maxCumulativeLevel = Math.max(count, playerCumulativeLevel);
        int rawMin = maxCumulativeLevel - 10;
        int minCumulativeLevel = Math.max(count, rawMin);
        maxCumulativeLevel = Math.max(minCumulativeLevel, maxCumulativeLevel);
        int targetCumulativeLevel = minCumulativeLevel + random.nextInt(maxCumulativeLevel - minCumulativeLevel + 1);

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

        Hero enemy = Hero.builder()
                .name(NAMES[random.nextInt(NAMES.length)])
                .startingClass(HeroClass.values()[random.nextInt(HeroClass.values().length)])
                .party(null)
                .build();

        enemy.setLevel(level);
        enemy.setAttack(6 + (level - 1) * 2);
        enemy.setDefense(1 + (level - 1));
        enemy.setHealth(60 + (level - 1) * 15);
        enemy.setMaxHealth(60 + (level - 1) * 15);
        enemy.setMana(0);
        enemy.setMaxMana(0);

        return enemy;
    }
}
