package com.university.project.legendsofswordandwand.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.university.project.legendsofswordandwand.model.Hero;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BattleServiceTest {

  @Autowired private BattleService battleService;

  @Test
  public void testAttackReducesDefenseCorrectly() {
    Hero attacker = Hero.builder().attack(15).build();
    Hero defender = Hero.builder().health(100).defense(5).build();

    battleService.executeAttack(attacker, defender);

    assertEquals(90, defender.getHealth());
  }
}
